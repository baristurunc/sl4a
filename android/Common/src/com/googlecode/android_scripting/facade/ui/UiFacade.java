/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.android_scripting.facade.ui;

import java.util.Queue;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.util.AndroidRuntimeException;


import com.googlecode.android_scripting.Sl4aApplication;
import com.googlecode.android_scripting.activity.ScriptingLayerServiceHelper;
import com.googlecode.android_scripting.exception.Sl4aRuntimeException;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.future.FutureResult;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

/**
 * UiFacade
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class UiFacade extends RpcReceiver {
  private final Service mService;
  private final Queue<FutureActivityTask<?>> mTaskQueue;
  private RunnableDialog mDialogTask;

  public UiFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mTaskQueue = ((Sl4aApplication) mService.getApplication()).getTaskQueue();
  }

  private void launchHelper() {
    Intent helper = new Intent(mService, ScriptingLayerServiceHelper.class);
    helper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mService.startActivity(helper);
  }

  @Rpc(description = "Create a spinner progress dialog.")
  public void dialogCreateSpinnerProgress(@RpcParameter(name = "Title") @RpcOptional String title,
      @RpcParameter(name = "Message") @RpcOptional String message,
      @RpcParameter(name = "Maximum progress") @RpcDefault("100") Integer max) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask =
        new RunnableProgressDialog(ProgressDialog.STYLE_SPINNER, max, title, message, true);
  }

  @Rpc(description = "Create a horizontal progress dialog.")
  public void dialogCreateHorizontalProgress(
      @RpcParameter(name = "Title") @RpcOptional String title,
      @RpcParameter(name = "Message") @RpcOptional String message,
      @RpcParameter(name = "Maximum progress") @RpcDefault("100") Integer max) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask =
        new RunnableProgressDialog(ProgressDialog.STYLE_HORIZONTAL, max, title, message, true);
  }

  @Rpc(description = "Create alert dialog.")
  public void dialogCreateAlert(@RpcParameter(name = "Title") @RpcOptional String title,
      @RpcParameter(name = "Message") @RpcOptional String message) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new RunnableAlertDialog(title, message);
  }

  @Rpc(description = "Create seek bar dialog.")
  public void dialogCreateSeekBar(
      @RpcParameter(name = "Starting value") @RpcDefault("50") Integer progress,
      @RpcParameter(name = "Maximum value") @RpcDefault("100") Integer max,
      @RpcParameter(name = "Title") String title, @RpcParameter(name = "Message") String message) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new RunnableSeekBarDialog(progress, max, title, message);
  }

  @Rpc(description = "Dismiss dialog.")
  public void dialogDismiss() {
    if (mDialogTask != null) {
      mDialogTask.dismissDialog();
      mDialogTask = null;
    }
  }

  @Rpc(description = "Show dialog.")
  public void dialogShow() throws InterruptedException {
    if (mDialogTask != null && mDialogTask.getDialog() == null) {
      mTaskQueue.offer((FutureActivityTask<?>) mDialogTask);
      launchHelper();
      mDialogTask.getShowLatch().await();
    } else {
      throw new AndroidRuntimeException("No dialog to show.");
    }
  }

  @Rpc(description = "Set progress dialog current value.")
  public void dialogSetCurrentProgress(@RpcParameter(name = "current") Integer current) {
    if (mDialogTask != null && mDialogTask instanceof RunnableProgressDialog) {
      ((ProgressDialog) mDialogTask.getDialog()).setProgress(current);
    } else {
      throw new Sl4aRuntimeException("No valid dialog to assign value to.");
    }
  }

  @Rpc(description = "Set progress dialog maximum value.")
  public void dialogSetMaxProgress(@RpcParameter(name = "max") Integer max) {
    if (mDialogTask != null && mDialogTask instanceof RunnableProgressDialog) {
      ((ProgressDialog) mDialogTask.getDialog()).setMax(max);
    } else {
      throw new Sl4aRuntimeException("No valid dialog to set maximum value of.");
    }
  }

  @Rpc(description = "Set alert dialog positive button text.")
  public void dialogSetPositiveButtonText(@RpcParameter(name = "text") String text) {
    if (mDialogTask != null && mDialogTask instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) mDialogTask).setPositiveButtonText(text);
    } else if (mDialogTask != null && mDialogTask instanceof RunnableSeekBarDialog) {
      ((RunnableSeekBarDialog) mDialogTask).setPositiveButtonText(text);
    } else {
      throw new AndroidRuntimeException("No dialog to add button to.");
    }
  }

  @Rpc(description = "Set alert dialog button text.")
  public void dialogSetNegativeButtonText(@RpcParameter(name = "text") String text) {
    if (mDialogTask != null && mDialogTask instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) mDialogTask).setNegativeButtonText(text);
    } else if (mDialogTask != null && mDialogTask instanceof RunnableSeekBarDialog) {
      ((RunnableSeekBarDialog) mDialogTask).setNegativeButtonText(text);
    } else {
      throw new AndroidRuntimeException("No dialog to add button to.");
    }
  }

  @Rpc(description = "Set alert dialog button text.")
  public void dialogSetNeutralButtonText(@RpcParameter(name = "text") String text) {
    if (mDialogTask != null && mDialogTask instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) mDialogTask).setNeutralButtonText(text);
    } else {
      throw new AndroidRuntimeException("No dialog to add button to.");
    }
  }

  // TODO(damonkohler): Make RPC layer translate between JSONArray and List<Object>.
  @Rpc(description = "Set alert dialog list items.")
  public void dialogSetItems(@RpcParameter(name = "items") JSONArray items) {
    if (mDialogTask != null && mDialogTask instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) mDialogTask).setItems(items);
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  @Rpc(description = "Set dialog single choice items and selected item.")
  public void dialogSetSingleChoiceItems(
      @RpcParameter(name = "items") JSONArray items,
      @RpcParameter(name = "selected", description = "selected item index") @RpcDefault("0") Integer selected) {
    if (mDialogTask != null && mDialogTask instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) mDialogTask).setSingleChoiceItems(items, selected);
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  @Rpc(description = "Set dialog multiple choice items and selection.")
  public void dialogSetMultiChoiceItems(
      @RpcParameter(name = "items") JSONArray items,
      @RpcParameter(name = "selected", description = "list of selected items") @RpcOptional JSONArray selected)
      throws JSONException {
    if (mDialogTask != null && mDialogTask instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) mDialogTask).setMultiChoiceItems(items, selected);
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  @Rpc(description = "Returns dialog response.")
  public Object dialogGetResponse() {
    try {
      FutureResult<Object> result = mDialogTask.getFutureResult();
      return result.get();
    } catch (Exception e) {
      throw new AndroidRuntimeException(e);
    }
  }

  @Rpc(description = "This method provides list of items user selected.", returns = "Selected items")
  public Set<Integer> dialogGetSelectedItems() {
    if (mDialogTask != null && mDialogTask instanceof RunnableAlertDialog) {
      return ((RunnableAlertDialog) mDialogTask).getSelectedItems();
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  @Override
  public void shutdown() {
  }
}
