package com.autoclickx.app;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.InputType;
import android.widget.*;
public class MainActivity extends Activity {
 private EditText interval;
 private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  ScrollView scroll=new ScrollView(this);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(24),dp(32),dp(24),dp(24));root.setBackgroundColor(0xff111820);scroll.addView(root);setContentView(scroll);
  TextView title=text("AutoClick X",32,0xff65e2b1);title.setTypeface(null,Typeface.BOLD);root.addView(title);
  root.addView(text("Автоматизация касаний • v0.1",16,Color.LTGRAY));
  TextView label=text("Интервал между нажатиями (мс)",18,Color.WHITE);label.setPadding(0,dp(28),0,0);root.addView(label);
  interval=new EditText(this);interval.setInputType(InputType.TYPE_CLASS_NUMBER);interval.setSingleLine(true);interval.setText(String.valueOf(getSharedPreferences("settings",MODE_PRIVATE).getInt("interval",500)));root.addView(interval);
  root.addView(text("50–60 000 мс. Изменения применяются при следующем запуске кликов.",14,Color.LTGRAY));
  Button save=new Button(this);save.setText("Сохранить интервал");root.addView(save);save.setOnClickListener(v->saveInterval());
  Button enable=new Button(this);enable.setText("Открыть спец. возможности");root.addView(enable);enable.setOnClickListener(v->{if(saveInterval())startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));});
  Button show=new Button(this);show.setText("Показать панель автокликера");root.addView(show);show.setOnClickListener(v->{if(saveInterval()){if(ClickService.instance!=null)ClickService.instance.showOverlay();else Toast.makeText(this,"Сначала включите службу AutoClick X",Toast.LENGTH_LONG).show();}});
  TextView help=text("1. Включите службу AutoClick X в специальных возможностях.\n\n2. Откройте нужное приложение и перетащите мишень.\n\n3. Нажмите «Старт». Мишень исчезнет на время кликов.\n\n4. Нажмите «Стоп», чтобы остановиться. «×» скрывает панель.",16,Color.WHITE);help.setPadding(0,dp(24),0,0);root.addView(help);
 }
 private boolean saveInterval(){try{int ms=Integer.parseInt(interval.getText().toString().trim());if(ms<50||ms>60000)throw new NumberFormatException();getSharedPreferences("settings",MODE_PRIVATE).edit().putInt("interval",ms).apply();Toast.makeText(this,"Интервал сохранён",Toast.LENGTH_SHORT).show();return true;}catch(NumberFormatException e){interval.setError("Введите число от 50 до 60 000");return false;}}
}
