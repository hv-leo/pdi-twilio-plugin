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

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Objects;


/**
 * Skeleton for PDI Step plugin.
 */
@Step( id = "SmsSender", image = "SmsSender.svg", name = "SMS Sender",
    description = "Send an SMS.", categoryDescription = "Output" )
public class SmsSenderMeta extends BaseStepMeta implements StepMetaInterface {
  
  private static Class<?> PKG = SmsSender.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private String accountSid;
  private String authToken;
  private String toField;
  private String fromField;
  private String messageField;
  private String statusField;
  private String priceField;
  private String errorCodeField;
  private String errorMessageField;

  public SmsSenderMeta() {
    super(); // allocate BaseStepMeta
  }

  public void setSuccessfulStepname( String successfulStepname ) {
    getStepIOMeta().getTargetStreams().get( 0 ).setSubject( successfulStepname );
  }

  public String getSuccessfulStepname() {
    return getTargetStepName( 0 );
  }

  public void setFailedStepname( String failedStepname ) {
    getStepIOMeta().getTargetStreams().get( 1 ).setSubject( failedStepname );
  }

  public String getFailedStepname() {
    return getTargetStepName( 1 );
  }

  private String getTargetStepName( int streamIndex ) {
    StreamInterface stream = getStepIOMeta().getTargetStreams().get( streamIndex );
    return java.util.stream.Stream.of( stream.getStepname(), stream.getSubject() )
      .filter( Objects::nonNull )
      .findFirst().map( Object::toString ).orElse( null );
  }

  public String getAccountSid() {
    return accountSid;
  }

  public void setAccountSid( String accountSid ) {
    this.accountSid = accountSid;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken( String authToken ) {
    this.authToken = authToken;
  }

  public String getToField() {
    return toField;
  }

  public void setToField( String toField ) {
    this.toField = toField;
  }

  public String getFromField() {
    return fromField;
  }

  public void setFromField( String fromField ) {
    this.fromField = fromField;
  }

  public String getMessageField() {
    return messageField;
  }

  public void setMessageField( String messageField ) {
    this.messageField = messageField;
  }

  public String getStatusField() {
    return statusField;
  }

  public void setStatusField( String statusField ) {
    this.statusField = statusField;
  }

  public String getErrorCodeField() {
    return errorCodeField;
  }

  public void setErrorCodeField( String errorCodeField ) {
    this.errorCodeField = errorCodeField;
  }

  public String getErrorMessageField() {
    return errorMessageField;
  }

  public void setErrorMessageField( String errorMessageField ) {
    this.errorMessageField = errorMessageField;
  }

  public String getPriceField() {
    return priceField;
  }

  public void setPriceField( String priceField ) {
    this.priceField = priceField;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }
  
  private void readData( Node stepnode ) {
    setSuccessfulStepname( XMLHandler.getTagValue( stepnode, "sendSuccessfulTo" ) );
    setFailedStepname( XMLHandler.getTagValue( stepnode, "sendFailedTo" ) );
    accountSid = XMLHandler.getTagValue( stepnode, "accountSid" );
    authToken = XMLHandler.getTagValue( stepnode, "authToken" );
    toField = XMLHandler.getTagValue( stepnode, "toField" );
    fromField = XMLHandler.getTagValue( stepnode, "fromField" );
    messageField = XMLHandler.getTagValue( stepnode, "messageField" );
    statusField = XMLHandler.getTagValue( stepnode, "statusField" );
    priceField = XMLHandler.getTagValue( stepnode, "priceField" );
    errorCodeField = XMLHandler.getTagValue( stepnode, "errorCodeField" );
    errorMessageField = XMLHandler.getTagValue( stepnode, "errorMessageField" );
  }

  public void setDefault() {
    statusField = "status";
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      setSuccessfulStepname( rep.getStepAttributeString( id_step, "sendSuccessfulTo" ) );
      setFailedStepname( rep.getStepAttributeString( id_step, "sendFailedTo" ) );
      accountSid = rep.getStepAttributeString( id_step, "accountSid" );
      authToken = rep.getStepAttributeString( id_step, "authToken" );
      toField = rep.getStepAttributeString( id_step, "toField" );
      fromField = rep.getStepAttributeString( id_step, "fromField" );
      messageField = rep.getStepAttributeString( id_step, "messageField" );
      statusField = rep.getStepAttributeString( id_step, "statusField" );
      priceField = rep.getStepAttributeString( id_step, "priceField" );
      errorCodeField = rep.getStepAttributeString( id_step, "errorCodeField" );
      errorMessageField = rep.getStepAttributeString( id_step, "errorMessageField" );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SmsSenderMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository" ), e );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( "    " + XMLHandler.addTagValue( "sendSuccessfulTo", getSuccessfulStepname() ) );
    retval.append( "    " + XMLHandler.addTagValue( "sendFailedTo", getFailedStepname() ) );
    retval.append( "    " + XMLHandler.addTagValue( "accountSid", accountSid ) );
    retval.append( "    " + XMLHandler.addTagValue( "authToken", authToken ) );
    retval.append( "    " + XMLHandler.addTagValue( "toField", toField ) );
    retval.append( "    " + XMLHandler.addTagValue( "fromField", fromField ) );
    retval.append( "    " + XMLHandler.addTagValue( "messageField", messageField ) );
    retval.append( "    " + XMLHandler.addTagValue( "statusField", statusField ) );
    retval.append( "    " + XMLHandler.addTagValue( "priceField", priceField ) );
    retval.append( "    " + XMLHandler.addTagValue( "errorCodeField", errorCodeField ) );
    retval.append( "    " + XMLHandler.addTagValue( "errorMessageField", errorMessageField ) );
    return retval.toString();
  }
  
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "sendSuccessfulTo", getSuccessfulStepname() );
      rep.saveStepAttribute( id_transformation, id_step, "sendFailedTo", getFailedStepname() );
      rep.saveStepAttribute( id_transformation, id_step, "accountSid", accountSid );
      rep.saveStepAttribute( id_transformation, id_step, "authToken", authToken );
      rep.saveStepAttribute( id_transformation, id_step, "toField", toField );
      rep.saveStepAttribute( id_transformation, id_step, "fromField", fromField );
      rep.saveStepAttribute( id_transformation, id_step, "messageField", messageField );
      rep.saveStepAttribute( id_transformation, id_step, "statusField", statusField );
      rep.saveStepAttribute( id_transformation, id_step, "priceField", priceField );
      rep.saveStepAttribute( id_transformation, id_step, "errorCodeField", errorCodeField );
      rep.saveStepAttribute( id_transformation, id_step, "errorMessageField", errorMessageField );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SmsSenderMeta.Exception.UnableToSaveStepInfoToRepository", id_step ), e );
    }
  }
  
  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, 
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    ValueMetaInterface valueMeta;

    if ( !Utils.isEmpty( statusField ) ) {
      valueMeta = new ValueMetaString( statusField );
      valueMeta.setOrigin( origin );
      rowMeta.addValueMeta( valueMeta );
    }

    if ( !Utils.isEmpty( priceField ) ) {
      valueMeta = new ValueMetaString( priceField );
      valueMeta.setOrigin( origin );
      rowMeta.addValueMeta( valueMeta );
    }

    if ( !Utils.isEmpty( errorCodeField ) ) {
      valueMeta = new ValueMetaInteger( errorCodeField );
      valueMeta.setOrigin( origin );
      rowMeta.addValueMeta( valueMeta );
    }

    if ( !Utils.isEmpty( errorMessageField ) ) {
      valueMeta = new ValueMetaString( errorMessageField );
      valueMeta.setOrigin( origin );
      rowMeta.addValueMeta( valueMeta );
    }
  }
  
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, 
    StepMeta stepMeta, RowMetaInterface prev, String input[], String output[],
    RowMetaInterface info, VariableSpace space, Repository repository, 
    IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString( PKG, "SmsSenderMeta.CheckResult.NotReceivingFields" ), stepMeta ); 
      remarks.add( cr );
    }
    else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG, "SmsSenderMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );  
      remarks.add( cr );
    }
    
    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG, "SmsSenderMeta.CheckResult.StepRecevingData2" ), stepMeta ); 
      remarks.add( cr );
    }
    else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG, "SmsSenderMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta ); 
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans ) {
    return new SmsSender( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new SmsSenderData();
  }

  @Override
  public boolean excludeFromCopyDistributeVerification() {
    return true;
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();
    for ( StreamInterface stream : targetStreams ) {
      stream.setStepMeta( StepMeta.findStep( steps, (String) stream.getSubject() ) );
    }
  }

  /**
   * Returns the Input/Output metadata for this step.
   */
  public StepIOMetaInterface getStepIOMeta() {
    StepIOMetaInterface ioMeta = super.getStepIOMeta( false );
    if ( ioMeta == null ) {
      ioMeta = new StepIOMeta( true, true, false, false, false, false );

      ioMeta.addStream( new Stream( StreamInterface.StreamType.TARGET, null, BaseMessages.getString(
        PKG, "SmsSenderMeta.InfoStream.Successful.Description" ), StreamIcon.TRUE, null ) );
      ioMeta.addStream( new Stream( StreamInterface.StreamType.TARGET, null, BaseMessages.getString(
        PKG, "SmsSenderMeta.InfoStream.Failed.Description" ), StreamIcon.FALSE, null ) );

      setStepIOMeta( ioMeta );
    }

    return ioMeta;
  }

  @Override
  public void resetStepIoMeta() {
  }

  /**
   * When an optional stream is selected, this method is called to handled the ETL metadata implications of that.
   *
   * @param stream
   *          The optional stream to handle.
   */
  public void handleStreamSelection( StreamInterface stream ) {
    // This step targets another step.
    // Make sure that we don't specify the same step for true and false...
    // If the user requests false, we blank out true and vice versa
    //
    List<StreamInterface> targets = getStepIOMeta().getTargetStreams();
    int index = targets.indexOf( stream );
    if ( index == 0 ) {
      // True
      //
      StepMeta failedStep = targets.get( 1 ).getStepMeta();
      if ( failedStep != null && failedStep.equals( stream.getStepMeta() ) ) {
        targets.get( 1 ).setStepMeta( null );
      }
    }
    if ( index == 1 ) {
      // False
      //
      StepMeta succesfulStep = targets.get( 0 ).getStepMeta();
      if ( succesfulStep != null && succesfulStep.equals( stream.getStepMeta() ) ) {
        targets.get( 0 ).setStepMeta( null );
      }
    }
  }

  public String getDialogClassName() {
    return "com.leonardo.coelho.SmsSenderDialog";
  }
}
