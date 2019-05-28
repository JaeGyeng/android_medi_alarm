
package com.example.jaegyengjo.medicine_alarm.Alarm.preferences;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.jaegyengjo.medicine_alarm.Alarm.Alarm;
import com.example.jaegyengjo.medicine_alarm.Alarm.BaseActivity;
import com.example.jaegyengjo.medicine_alarm.R;
import com.example.jaegyengjo.medicine_alarm.Alarm.database.Database;
import com.example.jaegyengjo.medicine_alarm.Alarm.preferences.AlarmPreference.Key;

public class AlarmPreferencesActivity extends BaseActivity {

	ImageButton deleteButton;
	TextView okButton;
	TextView cancelButton;
	private Alarm alarm;
	private MediaPlayer mediaPlayer;

	private ListAdapter listAdapter;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.alarm_preferences);

		Bundle bundle = getIntent().getExtras();

		if (bundle != null && bundle.containsKey("alarm")) {
			setMathAlarm((Alarm) bundle.getSerializable("alarm"));
		} else {
			setMathAlarm(new Alarm());
		}

		if (bundle != null && bundle.containsKey("adapter")) {
			setListAdapter((AlarmPreferenceListAdapter) bundle.getSerializable("adapter"));
		} else {
			setListAdapter(new AlarmPreferenceListAdapter(this, getMathAlarm()));
		}

		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				final AlarmPreferenceListAdapter alarmPreferenceListAdapter = (AlarmPreferenceListAdapter) getListAdapter();
				final AlarmPreference alarmPreference = (AlarmPreference) alarmPreferenceListAdapter.getItem(position);

				AlertDialog.Builder alert;
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				switch (alarmPreference.getType()) {
				case BOOLEAN:
					CheckedTextView checkedTextView = (CheckedTextView) v;
					boolean checked = !checkedTextView.isChecked();
					((CheckedTextView) v).setChecked(checked);
					switch (alarmPreference.getKey()) {
					case ALARM_ACTIVE:
						alarm.setAlarmActive(checked);
						break;
					case ALARM_VIBRATE:
						alarm.setVibrate(checked);
						if (checked) {
							Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
							vibrator.vibrate(1000);
						}
						break;
					}
					alarmPreference.setValue(checked);
					break;
				//메모
				case STRING:

					alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

					alert.setTitle(alarmPreference.getTitle());

					final EditText input = new EditText(AlarmPreferencesActivity.this);

					input.setText(alarmPreference.getValue().toString());

					alert.setView(input);
					alert.setPositiveButton("Ok", new OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							alarmPreference.setValue(input.getText().toString());

							if (alarmPreference.getKey() == Key.ALARM_NAME) {
								alarm.setAlarmName(alarmPreference.getValue().toString());
							}

							alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
							alarmPreferenceListAdapter.notifyDataSetChanged();
						}
					});
					alert.show();
					break;
				case LIST:
					alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

					alert.setTitle(alarmPreference.getTitle());

					CharSequence[] items = new CharSequence[alarmPreference.getOptions().length];
					for (int i = 0; i < items.length; i++)
						items[i] = alarmPreference.getOptions()[i];

					alert.setItems(items, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (alarmPreference.getKey()) {
							case ALARM_TONE:
								alarm.setAlarmTonePath(alarmPreferenceListAdapter.getAlarmTonePaths()[which]);
								if (alarm.getAlarmTonePath() != null) {
									if (mediaPlayer == null) {
										mediaPlayer = new MediaPlayer();
									} else {
										if (mediaPlayer.isPlaying())
											mediaPlayer.stop();
										mediaPlayer.reset();
									}
									try {
										mediaPlayer.setVolume(0.2f, 0.2f);
										mediaPlayer.setDataSource(AlarmPreferencesActivity.this, Uri.parse(alarm.getAlarmTonePath()));
										mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
										mediaPlayer.setLooping(false);
										mediaPlayer.prepare();
										mediaPlayer.start();

										if (alarmToneTimer != null)
											alarmToneTimer.cancel();
										alarmToneTimer = new CountDownTimer(3000, 3000) {
											@Override
											public void onTick(long millisUntilFinished) {

											}

											@Override
											public void onFinish() {
												try {
													if (mediaPlayer.isPlaying())
														mediaPlayer.stop();
												} catch (Exception e) {

												}
											}
										};
										alarmToneTimer.start();
									} catch (Exception e) {
										try {
											if (mediaPlayer.isPlaying())
												mediaPlayer.stop();
										} catch (Exception e2) {

										}
									}
								}
								break;
							default:
								break;
							}
							alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
							alarmPreferenceListAdapter.notifyDataSetChanged();
						}

					});

					alert.show();
					break;

				case MULTIPLE_LIST:
					alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

					alert.setTitle(alarmPreference.getTitle());


					CharSequence[] multiListItems = new CharSequence[alarmPreference.getOptions().length];
					for (int i = 0; i < multiListItems.length; i++)
						multiListItems[i] = alarmPreference.getOptions()[i];

					boolean[] checkedItems = new boolean[multiListItems.length];
					for (Alarm.Day day : getMathAlarm().getDays()) {
						checkedItems[day.ordinal()] = true;
					}
					alert.setMultiChoiceItems(multiListItems, checkedItems, new OnMultiChoiceClickListener() {

						@Override
						public void onClick(final DialogInterface dialog, int which, boolean isChecked) {

							Alarm.Day thisDay = Alarm.Day.values()[which];

							if (isChecked) {
								alarm.addDay(thisDay);
							} else {

								if (alarm.getDays().length > 1) {
									alarm.removeDay(thisDay);
								} else {

									((AlertDialog) dialog).getListView().setItemChecked(which, true);
								}
							}

						}
					});
					alert.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
							alarmPreferenceListAdapter.notifyDataSetChanged();

						}
					});
					alert.show();
					break;

				case TIME:
					TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmPreferencesActivity.this, new OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
							Calendar newAlarmTime = Calendar.getInstance();
							newAlarmTime.set(Calendar.HOUR_OF_DAY, hours);
							newAlarmTime.set(Calendar.MINUTE, minutes);
							newAlarmTime.set(Calendar.SECOND, 0);
							alarm.setAlarmTime(newAlarmTime);
							alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
							alarmPreferenceListAdapter.notifyDataSetChanged();
						}
					}, alarm.getAlarmTime().get(Calendar.HOUR_OF_DAY), alarm.getAlarmTime().get(Calendar.MINUTE), true);
					timePickerDialog.setTitle(alarmPreference.getTitle());
					timePickerDialog.show();
				default:
					break;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.findItem(R.id.menu_item_new).setVisible(false);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_save:
			Database.init(getApplicationContext());
			if (getMathAlarm().getId() < 1) {
				Database.create(getMathAlarm());
			} else {
				Database.update(getMathAlarm());
			}
			callMathAlarmScheduleService();
			finish();
			break;
		case R.id.menu_item_delete:
			AlertDialog.Builder dialog = new AlertDialog.Builder(AlarmPreferencesActivity.this);
			dialog.setTitle("삭제");
			dialog.setMessage("지우시겠습니까?");
			dialog.setPositiveButton("Ok", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					Database.init(getApplicationContext());
					if (getMathAlarm().getId() < 1) {
					} else {
						Database.deleteEntry(alarm);
						callMathAlarmScheduleService();
					}
					finish();
				}
			});
			dialog.setNegativeButton("Cancel", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			dialog.show();

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private CountDownTimer alarmToneTimer;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("alarm", getMathAlarm());
		outState.putSerializable("adapter", (AlarmPreferenceListAdapter) getListAdapter());
	};

	@Override
	protected void onPause() {
		super.onPause();
		try {
			if (mediaPlayer != null)
				mediaPlayer.release();
		} catch (Exception e) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public Alarm getMathAlarm() {
		return alarm;
	}

	public void setMathAlarm(Alarm alarm) {
		this.alarm = alarm;
	}

	public ListAdapter getListAdapter() {
		return listAdapter;
	}

	public void setListAdapter(ListAdapter listAdapter) {
		this.listAdapter = listAdapter;
		getListView().setAdapter(listAdapter);

	}

	public ListView getListView() {
		if (listView == null)
			listView = (ListView) findViewById(android.R.id.list);
		return listView;
	}

	public void setListView(ListView listView) {
		this.listView = listView;
	}

	@Override
	public void onClick(View v) {

	}



	public class AlarmPreferenceListAdapter extends BaseAdapter implements Serializable {

		private Context context;
		private Alarm alarm;
		private List<AlarmPreference> preferences = new ArrayList<AlarmPreference>();
		private final String[] repeatDays = {"일요일","월요일","화요일","수요일","목요일","금요일","토요일"};


		private String[] alarmTones;
		private String[] alarmTonePaths;


		public AlarmPreferenceListAdapter(Context context, Alarm alarm) {
			setContext(context);
			RingtoneManager ringtoneMgr = new RingtoneManager(getContext());
			ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
			Cursor alarmsCursor = ringtoneMgr.getCursor();

			alarmTones = new String[alarmsCursor.getCount()+1];
			alarmTones[0] = "무음";
			alarmTonePaths = new String[alarmsCursor.getCount()+1];
			alarmTonePaths[0] = "";

			if (alarmsCursor.moveToFirst()) {
				do {
					alarmTones[alarmsCursor.getPosition()+1] = ringtoneMgr.getRingtone(alarmsCursor.getPosition()).getTitle(getContext());
					alarmTonePaths[alarmsCursor.getPosition()+1] = ringtoneMgr.getRingtoneUri(alarmsCursor.getPosition()).toString();
				}while(alarmsCursor.moveToNext());
			}
			alarmsCursor.close();
			setMathAlarm(alarm);
		}

		@Override
		public int getCount() {
			return preferences.size();
		}

		@Override
		public Object getItem(int position) {
			return preferences.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AlarmPreference alarmPreference = (AlarmPreference) getItem(position);
			LayoutInflater layoutInflater = LayoutInflater.from(getContext());

			switch (alarmPreference.getType()) {

				case BOOLEAN:
					//레이아웃 설정
					if(null == convertView || convertView.getId() != android.R.layout.simple_list_item_checked)
						convertView = layoutInflater.inflate(android.R.layout.simple_list_item_checked, null);

					CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(android.R.id.text1);
					checkedTextView.setText(alarmPreference.getTitle());
					checkedTextView.setChecked((Boolean) alarmPreference.getValue());
					break;

				case INTEGER:
				case STRING:
				case LIST:
				case MULTIPLE_LIST:
				case TIME:
				default:
					if(null == convertView || convertView.getId() != android.R.layout.simple_list_item_2)
						convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);

					TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
					text1.setTextSize(18);
					text1.setText(alarmPreference.getTitle());

					TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
					text2.setText(alarmPreference.getSummary());
					break;
			}

			return convertView;
		}

		public Alarm getMathAlarm() {
			for(AlarmPreference preference : preferences){
				switch(preference.getKey()){
					case ALARM_ACTIVE:
						alarm.setAlarmActive((Boolean) preference.getValue());
						break;
					case ALARM_NAME:
						alarm.setAlarmName((String) preference.getValue());
						break;
					case ALARM_TIME:
						alarm.setAlarmTime((String) preference.getValue());
						break;

					case ALARM_TONE:
						alarm.setAlarmTonePath((String) preference.getValue());
						break;
					case ALARM_VIBRATE:
						alarm.setVibrate((Boolean) preference.getValue());
						break;
					case ALARM_REPEAT:
						alarm.setDays((Alarm.Day[]) preference.getValue());
						break;
				}
			}

			return alarm;
		}

		public void setMathAlarm(Alarm alarm) {
			this.alarm = alarm;
			preferences.clear();
			preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_ACTIVE,"활성화", null, null, alarm.getAlarmActive(), AlarmPreference.Type.BOOLEAN));
			preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_NAME, "약 이름",alarm.getAlarmName(), null, alarm.getAlarmName(), AlarmPreference.Type.STRING));
			preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TIME, "시간 설정",alarm.getAlarmTimeString(), null, alarm.getAlarmTime(), AlarmPreference.Type.TIME));
			preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_REPEAT, "요일 설정",alarm.getRepeatDaysString(), repeatDays, alarm.getDays(), AlarmPreference.Type.MULTIPLE_LIST));

			Uri alarmToneUri = Uri.parse(alarm.getAlarmTonePath());
			Ringtone alarmTone = RingtoneManager.getRingtone(getContext(), alarmToneUri);

			if(alarmTone instanceof Ringtone && !alarm.getAlarmTonePath().equalsIgnoreCase("")){
				preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TONE, "알람음", alarmTone.getTitle(getContext()),alarmTones, alarm.getAlarmTonePath(), AlarmPreference.Type.LIST));
			}else{
				preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TONE, "알람음", getAlarmTones()[0],alarmTones, null, AlarmPreference.Type.LIST));
			}

			preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_VIBRATE, "진동 설정",null, null, alarm.getVibrate(), AlarmPreference.Type.BOOLEAN));
		}


		public Context getContext() {
			return context;
		}

		public void setContext(Context context) {
			this.context = context;
		}

		public String[] getRepeatDays() {
			return repeatDays;
		}


		public String[] getAlarmTones() {
			return alarmTones;
		}

		public String[] getAlarmTonePaths() {
			return alarmTonePaths;
		}

	}



}
