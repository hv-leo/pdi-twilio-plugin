/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.leonardo.coelho;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.util.Arrays;
import java.util.List;

/**
 * Send SMS messages using Twilio Java API.
 * 
 */
public class SmsSender extends BaseStep implements StepInterface {

  private static Class<?> PKG = SmsSenderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private SmsSenderMeta meta;
  private SmsSenderData data;

  private static String STATUS_FAILED = "failed";

  public SmsSender( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Initialize and do work where other steps need to wait for...
   *
   * @param stepMetaInterface The metadata to work with
   * @param stepDataInterface The data to initialize
   */
  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    meta = (SmsSenderMeta) stepMetaInterface;
    data = (SmsSenderData) stepDataInterface;

    if ( super.init( stepMetaInterface, stepDataInterface ) ) {
      if ( Utils.isEmpty( meta.getAccountSid() ) ) {
        logError( BaseMessages.getString( PKG, "SmsSender.Missing.AccountSid" ) );
        return false;
      }
      if ( Utils.isEmpty( meta.getAuthToken() ) ) {
        logError( BaseMessages.getString( PKG, "SmsSender.Missing.AuthToken" ) );
        return false;
      }
      if ( Utils.isEmpty( meta.getToField() ) ) {
        logError( BaseMessages.getString( PKG, "SmsSender.Missing.To" ) );
        return false;
      }
      if ( Utils.isEmpty( meta.getFromField() ) ) {
        logError( BaseMessages.getString( PKG, "SmsSender.Missing.From" ) );
        return false;
      }
      if ( Utils.isEmpty( meta.getMessageField() ) ) {
        logError( BaseMessages.getString( PKG, "SmsSender.Missing.Message" ) );
        return false;
      }
      List<StreamInterface> targetStreams = meta.getStepIOMeta().getTargetStreams();
      data.chosesTargetSteps =
        targetStreams.get( 0 ).getStepMeta() != null || targetStreams.get( 1 ).getStepMeta() != null;
      return true;
    } else {
      return false;
    }
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) {
      // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      List fields = Arrays.asList( getInputRowMeta().getFieldNames( ) );

      // Mapping to field.
      data.toIdx = fields.indexOf( meta.getToField() );
      if ( data.toIdx < 0 ) {
        logError( BaseMessages.getString( PKG, "SmsSender.Invalid.To" ) );
        setErrors( 1 );
        stopAll();
        return false;
      }
      // Mapping from field.
      data.fromIdx = fields.indexOf( meta.getFromField() );
      if ( data.fromIdx < 0 ) {
        logError( BaseMessages.getString( PKG, "SmsSender.Invalid.From" ) );
        setErrors( 1 );
        stopAll();
        return false;
      }
      // Mapping message field.
      data.messageIdx = fields.indexOf( meta.getMessageField() );
      if ( data.messageIdx < 0 ) {
        logError( BaseMessages.getString( PKG, "SmsSender.Invalid.Message" ) );
        setErrors( 1 );
        stopAll();
        return false;
      }

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      data.startPoint = getInputRowMeta().size();
      r = RowDataUtil.resizeArray( r, data.outputRowMeta.size() );

      // Cache the position of the RowSet for the output.
      if ( data.chosesTargetSteps ) {
        List<StreamInterface> targetStreams = meta.getStepIOMeta().getTargetStreams();
        if ( !Utils.isEmpty( targetStreams.get( 0 ).getStepname() ) ) {
          data.successfulRowSet = findOutputRowSet( getStepname(), getCopy(), targetStreams.get( 0 ).getStepname(), 0 );
          if ( data.successfulRowSet == null ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "SmsSender.Log.TargetStepInvalid", targetStreams.get( 0 ).getStepname() ) );
          }
        } else {
          data.successfulRowSet = null;
        }

        if ( !Utils.isEmpty( targetStreams.get( 1 ).getStepname() ) ) {
          data.failedRowSet = findOutputRowSet( getStepname(), getCopy(), targetStreams.get( 1 ).getStepname(), 0 );
          if ( data.failedRowSet == null ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "SmsSender.Log.TargetStepInvalid", targetStreams.get( 1 ).getStepname() ) );
          }
        } else {
          data.failedRowSet = null;
        }
      }
    }

    // Get Twilio credentials.
    String accountSid = meta.getAccountSid();
    String authToken = meta.getAuthToken();

    // Get SMS values.
    String to = (String) r[data.toIdx ];
    String from = (String) r[data.fromIdx ];
    String message = (String) r[data.messageIdx ];

    if ( Utils.isEmpty( accountSid ) ) {
      logError( BaseMessages.getString( PKG, "SmsSender.Null.AccountSid" ) );
      putFailedTransferRow( r );
      return true;
    }
    if ( Utils.isEmpty( authToken ) ) {
      logError( BaseMessages.getString( PKG, "SmsSender.Null.AuthToken" ) );
      putFailedTransferRow( r );
      return true;
    }
    if ( Utils.isEmpty( to ) ) {
      logError( BaseMessages.getString( PKG, "SmsSender.Null.To" ) );
      putFailedTransferRow( r );
      return true;
    }
    if ( Utils.isEmpty( from ) ) {
      logError( BaseMessages.getString( PKG, "SmsSender.Null.From" ) );
      putFailedTransferRow( r );
      return true;
    }
    if ( Utils.isEmpty( message ) ) {
      logError( BaseMessages.getString( PKG, "SmsSender.Null.Message" ) );
      putFailedTransferRow( r );
      return true;
    }

    // Send SMS message.
    Twilio.init( accountSid, authToken );
    PhoneNumber receiver = new PhoneNumber( to );
    PhoneNumber sender = new PhoneNumber( from );
    Message sms;
    try {
      sms = Message.creator( receiver, sender, message ).create();
    } catch( ApiException ex ) {
      logError( BaseMessages.getString( PKG, "SmsSender.Message.Creation.Failed", ex.getMessage() ) );
      putFailedTransferRow( r );
      return true;
    }

    // Add SMS output fields.
    int idx = data.startPoint;
    if ( !Utils.isEmpty( meta.getStatusField() ) ) {
      r[ idx++ ] = sms.getStatus().toString();
    }
    if ( !Utils.isEmpty( meta.getPriceField() ) ) {
      r[ idx++ ] = sms.getPrice();
    }
    if ( !Utils.isEmpty( meta.getErrorCodeField() ) ) {
      r[ idx++ ] = sms.getErrorCode();
    }
    if ( !Utils.isEmpty( meta.getErrorMessageField() ) ) {
      r[ idx++ ] = sms.getErrorMessage();
    }

    // Failure while sending SMS message.
    if ( sms.getStatus().equals( STATUS_FAILED ) ) {
      putFailedTransferRow( r );
    }
    // SMS message was successfully sent.
    putSuccessfulTransferRow( r );

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() )
        logBasic( BaseMessages.getString( PKG, "SmsSender.Log.LineNumber" ) + getLinesRead() );
    }
      
    return true;
  }

  private void putFailedTransferRow( Object[] r ) throws KettleStepException {
    if ( !data.chosesTargetSteps ) {
      putRow( data.outputRowMeta, r );
    } else {
      putRowTo( data.outputRowMeta, r, data.failedRowSet );
    }
  }

  private void putSuccessfulTransferRow( Object[] r ) throws KettleStepException {
    if ( !data.chosesTargetSteps ) {
      putRow( data.outputRowMeta, r );
    } else {
      putRowTo( data.outputRowMeta, r, data.successfulRowSet );
    }
  }
}