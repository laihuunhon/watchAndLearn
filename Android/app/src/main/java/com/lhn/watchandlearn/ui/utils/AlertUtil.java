package com.lhn.watchandlearn.ui.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import com.lhn.watchandlearn.R;

public class AlertUtil {

	static public interface ConfirmAlertListener {

		void onOkClicked(int requiestedCode);
	}

	static public interface ConfirmInputAlertListener {

		void onOkClicked(String text, int requiestedCode);
	}

	static public void showAlert(Context context, String title, String sms) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(title).setMessage(sms).setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	static public void showAlert(Context context, String title, int smsID) {
		showAlert(context, title, smsID, null);
	}

	static public void showAlert(Context context, String title, int smsID, final IDialogCallback aCallback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(title).setMessage(smsID).setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();

				if (aCallback != null) {
					aCallback.onClick(dialog, id);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	static public void showAlert(Context context, int title, int smsID, final IDialogCallback aCallback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(title).setMessage(smsID).setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();

				if (aCallback != null) {
					aCallback.onClick(dialog, id);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	static public void showAlert(Context context, int titleId, int smsID) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(titleId).setMessage(smsID).setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	static class OnDialogAlertClickListener implements DialogInterface.OnClickListener {

		ConfirmAlertListener listener;
		int requestCode;

		public OnDialogAlertClickListener(ConfirmAlertListener listener, int requestCode) {
			this.listener = listener;
			this.requestCode = requestCode;
		}

		public void onClick(DialogInterface dialog, int id) {
			listener.onOkClicked(requestCode);
		}
	}

	static class OnDialogInputAlertClickListener implements DialogInterface.OnClickListener {

		ConfirmInputAlertListener listener;
		int requestCode;
		EditText editText;

		public OnDialogInputAlertClickListener(ConfirmInputAlertListener listener, EditText aEditText, int requestCode) {
			this.listener = listener;
			this.requestCode = requestCode;
			this.editText = aEditText;
		}

		public void onClick(DialogInterface dialog, int id) {
			listener.onOkClicked(editText.getText().toString().trim(), requestCode);
		}
	}

	static public void showConfirmAlert(Context context, String sms, String okLabel, ConfirmAlertListener listener, int requestCode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(sms).setCancelable(false).setPositiveButton(okLabel, new OnDialogAlertClickListener(listener, requestCode)).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();

		alert.show();

	}

	static public void showConfirmAlert(Context context, String title, String sms, String okLabel, ConfirmAlertListener listener, int requestCode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(sms).setCancelable(false).setPositiveButton(okLabel, new OnDialogAlertClickListener(listener, requestCode)).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		if (title != null && title.trim().length() > 0) {
			builder.setTitle(title);
		}

		AlertDialog alert = builder.create();

		alert.show();

	}

	static public void showConfirmAlert(Context context, String title, String sms, String cancelLabel, String okLabel, ConfirmAlertListener listener, int requestCode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(sms).setCancelable(false).setPositiveButton(okLabel, new OnDialogAlertClickListener(listener, requestCode)).setNegativeButton(cancelLabel, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		if (title != null && title.trim().length() > 0) {
			builder.setTitle(title);
		}

		AlertDialog alert = builder.create();

		alert.show();

	}

	static public void showConfirmAlert(Context context, String sms, ConfirmAlertListener listener, int requestCode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(sms).setCancelable(false).setPositiveButton(R.string.ok, new OnDialogAlertClickListener(listener, requestCode)).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();

		alert.show();

	}

	static public void showInputConfirmAlert(Context context, String inputText, ConfirmInputAlertListener listener, int requestCode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		EditText editText = new EditText(context);

		editText.setText(inputText);

		builder.setView(editText);
		builder.setCancelable(false).setPositiveButton(R.string.ok, new OnDialogInputAlertClickListener(listener, editText, requestCode)).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		AlertDialog alert = builder.create();

		alert.show();

	}

	public static interface IDialogCallback {

		public void onClick(DialogInterface dialog, int id);
	}
}
