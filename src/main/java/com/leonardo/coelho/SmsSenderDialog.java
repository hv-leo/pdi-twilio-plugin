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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.Arrays;
import java.util.List;

public class SmsSenderDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = SmsSenderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private static final int MARGIN_SIZE = 15;
  private static final int ELEMENT_SPACING = Const.MARGIN;

  private SmsSenderMeta meta;

  private ScrolledComposite scrolledComposite;
  private Composite contentComposite;

  // Step name.
  private Label wStepNameLabel;
  private Text wStepNameField;

  // Target step for successful transfers.
  private Label wlSuccessfulToLabel;
  private CCombo wSuccessfulToField;

  // Target step for failed transfers.
  private Label wlFailedToLabel;
  private CCombo wFailedToField;

  // Twilio - credentials.
  private Group credentialsGroup;

  // Twilio - account sid.
  private Label wAccountSidLabel;
  private Text wAccountSidField;

  // Twilio - auth token.
  private Label wAuthTokenLabel;
  private Text wAuthTokenField;

  // Twilio - SMS message.
  private Group smsGroup;

  // Twilio - receiver phone number.
  private Label wToLabel;
  private CCombo wToField;

  // Twilio - sender phone number.
  private Label wFromLabel;
  private CCombo wFromField;

  // Twilio - message content.
  private Label wMessageLabel;
  private CCombo wMessageField;

  // Output fields.
  private Group outputGroup;

  // SMS status.
  private Label wStatusLabel;
  private Text wStatusField;

  // SMS price.
  private Label wPriceLabel;
  private Text wPriceField;

  // SMS status.
  private Label wErrorCodeLabel;
  private Text wErrorCodeField;

  // SMS status.
  private Label wErrorMessageLabel;
  private Text wErrorMessageField;

  // Listeners
  private ModifyListener lsMod;
  private Listener lsCancel;
  private Listener lsOK;
  private SelectionAdapter lsDef;
  private boolean changed;

  public SmsSenderDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    meta = (SmsSenderMeta) in;
  }

  public String open() {
    // Set up window
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, meta );
    int middle = props.getMiddlePct();

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        meta.setChanged();
      }
    };
    changed = meta.hasChanged();

    // 15 pixel margins
    FormLayout formLayout = new FormLayout();
    formLayout.marginLeft = MARGIN_SIZE;
    formLayout.marginHeight = MARGIN_SIZE;
    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "SmsSenderDialog.Shell.Title" ) );

    // Build a scrolling composite and a composite for holding all content
    scrolledComposite = new ScrolledComposite( shell, SWT.V_SCROLL );
    contentComposite = new Composite( scrolledComposite, SWT.NONE );
    FormLayout contentLayout = new FormLayout();
    contentLayout.marginRight = MARGIN_SIZE;
    contentComposite.setLayout( contentLayout );
    FormData compositeLayoutData = new FormDataBuilder().fullSize()
      .result();
    contentComposite.setLayoutData( compositeLayoutData );
    props.setLook( contentComposite );

    // Step name label and text field.
    wStepNameLabel = new Label( contentComposite, SWT.RIGHT );
    wStepNameLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.Stepname.Label" ) );
    props.setLook( wStepNameLabel );
    FormData fdStepNameLabel = new FormDataBuilder().left()
      .top()
      .right( middle, -ELEMENT_SPACING )
      .result();
    wStepNameLabel.setLayoutData( fdStepNameLabel );

    wStepNameField = new Text( contentComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepNameField.setText( stepname );
    props.setLook( wStepNameField );
    wStepNameField.addModifyListener( lsMod );
    FormData fdStepName = new FormDataBuilder().left( middle, 0 )
      .top( )
      .right( 100, 0 )
      .result();
    wStepNameField.setLayoutData( fdStepName );

    // Spacer between entry info and content.
    Label topSpacer = new Label( contentComposite, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdSpacer = new FormDataBuilder().fullWidth()
      .top( wStepNameField, MARGIN_SIZE )
      .result();
    topSpacer.setLayoutData( fdSpacer );


    // Send Successful Transfers to...
    wlSuccessfulToLabel = new Label( contentComposite, SWT.RIGHT );
    props.setLook( wlSuccessfulToLabel );
    wlSuccessfulToLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.Successful.Label" ) );
    FormData suclTransformation = new FormDataBuilder().left()
      .top( topSpacer, ELEMENT_SPACING )
      .right( middle, -ELEMENT_SPACING )
      .result();
    wlSuccessfulToLabel.setLayoutData( suclTransformation );

    wSuccessfulToField = new CCombo( contentComposite, SWT.BORDER );
    props.setLook( wSuccessfulToField );
    wSuccessfulToField.addModifyListener( lsMod );
    FormData sufTransformation = new FormDataBuilder().left( middle, 0 )
      .top( topSpacer, ELEMENT_SPACING )
      .right( 100, 0 )
      .result();
    wSuccessfulToField.setLayoutData( sufTransformation );

    // Send Failed Transfers to...
    wlFailedToLabel = new Label( contentComposite, SWT.RIGHT );
    props.setLook( wlFailedToLabel );
    wlFailedToLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.Failed.Label" ) );
    FormData faillTransformation = new FormDataBuilder().left()
      .top( wSuccessfulToField, ELEMENT_SPACING )
      .right( middle, -ELEMENT_SPACING )
      .result();
    wlFailedToLabel.setLayoutData( faillTransformation );

    wFailedToField = new CCombo( contentComposite, SWT.BORDER );
    props.setLook( wFailedToField );
    wFailedToField.addModifyListener( lsMod );
    FormData failTransformation = new FormDataBuilder().left( middle, 0 )
      .top( wSuccessfulToField, ELEMENT_SPACING )
      .right( 100, 0 )
      .result();
    wFailedToField.setLayoutData( failTransformation );

    // Group for Twilio credentials fields.
    credentialsGroup = new Group( contentComposite, SWT.SHADOW_ETCHED_IN );
    credentialsGroup.setText( BaseMessages.getString( PKG, "SmsSenderDialog.Credentials.GroupText" ) );
    FormLayout groupLayout = new FormLayout();
    groupLayout.marginWidth = MARGIN_SIZE;
    groupLayout.marginHeight = MARGIN_SIZE;
    credentialsGroup.setLayout( groupLayout );
    FormData groupLayoutData = new FormDataBuilder().fullWidth()
      .top( wFailedToField, MARGIN_SIZE )
      .result();
    credentialsGroup.setLayoutData( groupLayoutData );
    props.setLook( credentialsGroup );

    // Account Sid label/field
    wAccountSidLabel = new Label( credentialsGroup, SWT.RIGHT );
    props.setLook( wAccountSidLabel );
    wAccountSidLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.AccountSid.Label" ) );
    FormData fdlTransformation = new FormDataBuilder().left()
      .top()
      .right( middle, -ELEMENT_SPACING )
      .result();
    wAccountSidLabel.setLayoutData( fdlTransformation );

    wAccountSidField = new Text( credentialsGroup, SWT.BORDER );
    props.setLook( wAccountSidField );
    wAccountSidField.addModifyListener( lsMod );
    FormData fdTransformation = new FormDataBuilder().left( middle, 0 )
      .top()
      .right( 100, 0 )
      .result();
    wAccountSidField.setLayoutData( fdTransformation );

    // Auth Token label/field
    wAuthTokenLabel = new Label( credentialsGroup, SWT.RIGHT );
    props.setLook( wAuthTokenLabel );
    wAuthTokenLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.AuthToken.Label" ) );
    FormData fdlTransformation2 = new FormDataBuilder().left()
      .top( wAccountSidField, ELEMENT_SPACING )
      .right( middle, -ELEMENT_SPACING )
      .result();
    wAuthTokenLabel.setLayoutData( fdlTransformation2 );

    wAuthTokenField = new Text( credentialsGroup, SWT.BORDER );
    props.setLook( wAuthTokenField );
    wAuthTokenField.addModifyListener( lsMod );
    FormData fdTransformation2 = new FormDataBuilder().left( middle, 0 )
      .top( wAccountSidField, ELEMENT_SPACING )
      .right( 100, 0 )
      .result();
    wAuthTokenField.setLayoutData( fdTransformation2 );

    // Group for SMS message fields.
    smsGroup = new Group( contentComposite, SWT.SHADOW_ETCHED_IN );
    smsGroup.setText( BaseMessages.getString( PKG, "SmsSenderDialog.SMS.GroupText" ) );
    FormLayout groupLayout2 = new FormLayout();
    groupLayout2.marginWidth = MARGIN_SIZE;
    groupLayout2.marginHeight = MARGIN_SIZE;
    smsGroup.setLayout( groupLayout2 );
    FormData groupLayoutData2 = new FormDataBuilder().fullWidth()
      .top( credentialsGroup, MARGIN_SIZE )
      .result();
    smsGroup.setLayoutData( groupLayoutData2 );
    props.setLook( smsGroup );

    // Receiver phone number label/field
    wToLabel = new Label( smsGroup, SWT.RIGHT );
    props.setLook( wToLabel );
    wToLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.To.Label" ) );
    FormData fdlTransformation3 = new FormDataBuilder().left()
      .top()
      .right( middle, -ELEMENT_SPACING )
      .result();
    wToLabel.setLayoutData( fdlTransformation3 );

    wToField = new CCombo( smsGroup, SWT.BORDER );
    props.setLook( wToField );
    wToField.addModifyListener( lsMod );
    FormData fdTransformation3 = new FormDataBuilder().left( middle, 0 )
      .top()
      .right( 100, 0 )
      .result();
    wToField.setLayoutData( fdTransformation3 );

    // Sender phone number label/field
    wFromLabel = new Label( smsGroup, SWT.RIGHT );
    props.setLook( wFromLabel );
    wFromLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.From.Label" ) );
    FormData fdlTransformation4 = new FormDataBuilder().left()
      .top( wToField, ELEMENT_SPACING )
      .right( middle, -ELEMENT_SPACING )
      .result();
    wFromLabel.setLayoutData( fdlTransformation4 );

    wFromField = new CCombo( smsGroup, SWT.BORDER );
    props.setLook( wFromField );
    wFromField.addModifyListener( lsMod );
    FormData fdTransformation4 = new FormDataBuilder().left( middle, 0 )
      .top( wToField, ELEMENT_SPACING )
      .right( 100, 0 )
      .result();
    wFromField.setLayoutData( fdTransformation4 );

    // SMS message content label/field
    wMessageLabel = new Label( smsGroup, SWT.RIGHT );
    props.setLook( wMessageLabel );
    wMessageLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.Message.Label" ) );
    FormData fdlTransformation5 = new FormDataBuilder().left()
      .top( wFromField, ELEMENT_SPACING )
      .right( middle, -ELEMENT_SPACING )
      .result();
    wMessageLabel.setLayoutData( fdlTransformation5 );

    wMessageField = new CCombo( smsGroup, SWT.BORDER );
    props.setLook( wMessageField );
    wMessageField.addModifyListener( lsMod );
    FormData fdTransformation5 = new FormDataBuilder().left( middle, 0 )
      .top( wFromField, ELEMENT_SPACING )
      .right( 100, 0 )
      .result();
    wMessageField.setLayoutData( fdTransformation5 );

    // Group for output fields.
    outputGroup = new Group( contentComposite, SWT.SHADOW_ETCHED_IN );
    outputGroup.setText( BaseMessages.getString( PKG, "SmsSenderDialog.Output.GroupText" ) );
    FormLayout groupLayout3 = new FormLayout();
    groupLayout3.marginWidth = MARGIN_SIZE;
    groupLayout3.marginHeight = MARGIN_SIZE;
    outputGroup.setLayout( groupLayout3 );
    FormData groupLayoutData3 = new FormDataBuilder().fullWidth()
      .top( smsGroup, MARGIN_SIZE )
      .result();
    outputGroup.setLayoutData( groupLayoutData3 );
    props.setLook( outputGroup );

    // SMS status label/field
    wStatusLabel = new Label( outputGroup, SWT.RIGHT );
    props.setLook( wStatusLabel );
    wStatusLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.Status.Label" ) );
    FormData fdlTransformation6 = new FormDataBuilder().left()
      .top()
      .right( middle, -ELEMENT_SPACING )
      .result();
    wStatusLabel.setLayoutData( fdlTransformation6 );

    wStatusField = new Text( outputGroup, SWT.BORDER );
    props.setLook( wStatusField );
    wStatusField.addModifyListener( lsMod );
    FormData fdTransformation6 = new FormDataBuilder().left( middle, 0 )
      .top( )
      .right( 100, 0 )
      .result();
    wStatusField.setLayoutData( fdTransformation6 );

    // SMS price label/field
    wPriceLabel = new Label( outputGroup, SWT.RIGHT );
    props.setLook( wPriceLabel );
    wPriceLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.Price.Label" ) );
    FormData fdlTransformation7 = new FormDataBuilder().left()
      .top( wStatusField, ELEMENT_SPACING )
      .right( middle, -ELEMENT_SPACING )
      .result();
    wPriceLabel.setLayoutData( fdlTransformation7 );

    wPriceField = new Text( outputGroup, SWT.BORDER );
    props.setLook( wPriceField );
    wPriceField.addModifyListener( lsMod );
    FormData fdTransformation7 = new FormDataBuilder().left( middle, 0 )
      .top( wStatusField, ELEMENT_SPACING )
      .right( 100, 0 )
      .result();
    wPriceField.setLayoutData( fdTransformation7 );

    // SMS error code label/field
    wErrorCodeLabel = new Label( outputGroup, SWT.RIGHT );
    props.setLook( wErrorCodeLabel );
    wErrorCodeLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.ErrorCode.Label" ) );
    FormData fdlTransformation8 = new FormDataBuilder().left()
      .top( wPriceField, ELEMENT_SPACING )
      .right( middle, -ELEMENT_SPACING )
      .result();
    wErrorCodeLabel.setLayoutData( fdlTransformation8 );

    wErrorCodeField = new Text( outputGroup, SWT.BORDER );
    props.setLook( wErrorCodeField );
    wErrorCodeField.addModifyListener( lsMod );
    FormData fdTransformation8 = new FormDataBuilder().left( middle, 0 )
      .top( wPriceField, ELEMENT_SPACING )
      .right( 100, 0 )
      .result();
    wErrorCodeField.setLayoutData( fdTransformation8 );

    // SMS error message label/field
    wErrorMessageLabel = new Label( outputGroup, SWT.RIGHT );
    props.setLook( wErrorMessageLabel );
    wErrorMessageLabel.setText( BaseMessages.getString( PKG, "SmsSenderDialog.ErrorMessage.Label" ) );
    FormData fdlTransformation9 = new FormDataBuilder().left()
      .top( wErrorCodeField, ELEMENT_SPACING )
      .right( middle, -ELEMENT_SPACING )
      .result();
    wErrorMessageLabel.setLayoutData( fdlTransformation9 );

    wErrorMessageField = new Text( outputGroup, SWT.BORDER );
    props.setLook( wErrorCodeField );
    wErrorMessageField.addModifyListener( lsMod );
    FormData fdTransformation9 = new FormDataBuilder().left( middle, 0 )
      .top( wErrorCodeField, ELEMENT_SPACING )
      .right( 100, 0 )
      .result();
    wErrorMessageField.setLayoutData( fdTransformation9 );

    // Cancel, action and OK buttons for the bottom of the window.
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    FormData fdCancel = new FormDataBuilder().right( 100, -MARGIN_SIZE )
      .bottom()
      .result();
    wCancel.setLayoutData( fdCancel );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    FormData fdOk = new FormDataBuilder().right( wCancel, -ELEMENT_SPACING )
      .bottom()
      .result();
    wOK.setLayoutData( fdOk );

    // Space between bottom buttons and and group content.
    Label bottomSpacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdhSpacer = new FormDataBuilder().left()
      .right( 100, -MARGIN_SIZE )
      .bottom( wCancel, -MARGIN_SIZE )
      .result();
    bottomSpacer.setLayoutData( fdhSpacer );

    // Add everything to the scrolling composite
    scrolledComposite.setContent( contentComposite );
    scrolledComposite.setExpandVertical( true );
    scrolledComposite.setExpandHorizontal( true );
    scrolledComposite.setMinSize( contentComposite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );

    scrolledComposite.setLayout( new FormLayout() );
    FormData fdScrolledComposite = new FormDataBuilder().fullWidth()
      .top()
      .bottom( bottomSpacer, -MARGIN_SIZE )
      .result();
    scrolledComposite.setLayoutData( fdScrolledComposite );
    props.setLook( scrolledComposite );

    // Listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wStepNameField.addSelectionListener( lsDef );

    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Show shell
    setSize();

    // Populate Window.
    getData();
    meta.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    // Add target steps to 'send to' combo box options.
    StepMeta stepinfo = transMeta.findStep( stepname );
    if ( stepinfo != null ) {
      List<StepMeta> nextSteps = transMeta.findNextSteps( stepinfo );
      nextSteps.stream().forEach( stepMeta -> {
        wSuccessfulToField.add( stepMeta.getName() );
        wFailedToField.add( stepMeta.getName() );
      } );
    }

    // Get 'send to' steps.
    wSuccessfulToField.setText( Const.NVL( meta.getSuccessfulStepname( ), "" ) );
    wFailedToField.setText( Const.NVL( meta.getFailedStepname( ), "" ) );

    // Add previous fields to transfer combo box options.
    try {
      String[] prevFields = transMeta.getPrevStepFields( stepname ).getFieldNames();
      Arrays.stream( prevFields ).forEach( field -> {
        wToField.add( field );
        wFromField.add( field );
        wMessageField.add( field );
      } );
    } catch ( KettleStepException e ) {
      e.printStackTrace();
    }

    // Get credentials fields values.
    String accountSidField = meta.getAccountSid();
    if ( accountSidField != null ) {
      wAccountSidField.setText( accountSidField );
    }
    String authTokenField = meta.getAuthToken();
    if ( authTokenField != null ) {
      wAuthTokenField.setText( authTokenField );
    }

    // Get SMS fields values.
    String toField = meta.getToField();
    if ( toField != null ) {
      wToField.setText( toField );
    }
    String fromField = meta.getFromField();
    if ( fromField != null ) {
      wFromField.setText( fromField );
    }
    String messageField = meta.getMessageField();
    if ( messageField != null ) {
      wMessageField.setText( messageField );
    }

    // Get output fields
    String statusField = meta.getStatusField();
    if ( statusField != null ) {
      wStatusField.setText( statusField );
    }
    String priceField = meta.getPriceField();
    if ( priceField != null ) {
      wPriceField.setText( priceField );
    }
    String errorCodeField = meta.getErrorCodeField();
    if ( errorCodeField != null ) {
      wErrorCodeField.setText( errorCodeField );
    }String errorMessageField = meta.getErrorMessageField();
    if ( errorMessageField != null ) {
      wErrorMessageField.setText( errorMessageField );
    }
  }

  /**
   * Save information from dialog fields to the meta-data input.
   */
  private void getMeta( SmsSenderMeta meta ) {
    // Set target streams.
    List<StreamInterface> targetStreams = meta.getStepIOMeta().getTargetStreams();
    String successfulStream = wSuccessfulToField.getText();
    String failedStream = wFailedToField.getText();
    targetStreams.get( 0 ).setStepMeta( transMeta.findStep( successfulStream ) );
    targetStreams.get( 1 ).setStepMeta( transMeta.findStep( failedStream ) );
    meta.setSuccessfulStepname( successfulStream );
    meta.setFailedStepname( failedStream );

    meta.setAccountSid( wAccountSidField.getText() );
    meta.setAuthToken( wAuthTokenField.getText() );
    meta.setToField( wToField.getText() );
    meta.setFromField( wFromField.getText() );
    meta.setMessageField( wMessageField.getText() );
    meta.setStatusField( wStatusField.getText() );
    meta.setPriceField( wPriceField.getText() );
    meta.setErrorCodeField( wErrorCodeField.getText() );
    meta.setErrorMessageField( wErrorMessageField.getText() );
  }

  private void cancel() {
    dispose();
  }

  private void ok() {
    getMeta( meta );
    stepname = wStepNameField.getText();
    dispose();
  }
}