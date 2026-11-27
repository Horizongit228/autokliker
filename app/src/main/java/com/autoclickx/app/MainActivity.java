package com.autoclickx.app;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
public class MainActivity extends Activity {
 private static final int BG=0xff0b111c,CARD=0xff172131,FG=0xfff0f5ff,MUTED=0xff9caec5,MINT=0xffa7f3d0,INK=0xff10251d;
 private EditText interval; private TextView status,rate; private Button show;
 private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
 private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
 private void space(LinearLayout parent,int h){View v=new View(this);parent.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
 private LinearLayout card(LinearLayout parent){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(18),dp(18),dp(18),dp(18));c.setBackground(shape(CARD,24));parent.addView(c,new LinearLayout.LayoutParams(-1,-2));return c;}
 private Button button(String s,int bg,int fg){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(fg);b.setTypeface(null,Typeface.BOLD);b.setPadding(dp(6),0,dp(6),0);b.setMinHeight(0);b.setMinimumHeight(0);b.setMinWidth(0);b.setMinimumWidth(0);b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x337faaa0),shape(bg,15),null));return b;}
 private void full(LinearLayout parent,Button b){parent.addView(b,new LinearLayout.LayoutParams(-1,dp(52)));}
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(20),dp(26),dp(20),dp(28));scroll.addView(root);setContentView(scroll);
  root.addView(text("AUTOCLICK  /  X",12,MINT,true));space(root,8);root.addView(text("Твои касания.\nТвой ритм.",32,FG,true));space(root,8);root.addView(text("Автоматизация экрана  •  v0.4",14,MUTED,false));space(root,22);
  LinearLayout access=card(root);status=text("",17,MINT,true);access.addView(status);space(access,8);access.addView(text("Для касаний требуется служба специальных возможностей.",14,MUTED,false));space(access,14);
  Button enable=button("Настроить доступ",0xff28384d,FG);full(access,enable);enable.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
  space(root,14);LinearLayout settings=card(root);settings.addView(text("Интервал нажатий",19,FG,true));space(settings,10);
  LinearLayout field=new LinearLayout(this);field.setGravity(Gravity.CENTER_VERTICAL);field.setPadding(dp(12),0,dp(12),0);field.setBackground(shape(BG,16));settings.addView(field,new LinearLayout.LayoutParams(-1,dp(76)));
  interval=new EditText(this);interval.setTextColor(MINT);interval.setTextSize(34);interval.setTypeface(null,Typeface.BOLD);interval.setInputType(InputType.TYPE_CLASS_NUMBER);interval.setSingleLine(true);interval.setBackgroundColor(0x00000000);interval.setPadding(0,0,0,0);interval.setText(String.valueOf(getSharedPreferences("settings",MODE_PRIVATE).getInt("interval",500)));field.addView(interval,new LinearLayout.LayoutParams(0,-1,1));field.addView(text("мс",19,MUTED,true));space(settings,12);
  LinearLayout presets=new LinearLayout(this);settings.addView(presets);for(int ms:new int[]{1,10,50,100,500}){Button b=button(String.valueOf(ms),0xff28384d,FG);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(42),1);lp.setMargins(dp(2),0,dp(2),0);presets.addView(b,lp);b.setOnClickListener(v->interval.setText(String.valueOf(ms)));}
  space(settings,12);rate=text("",14,MUTED,false);settings.addView(rate);interval.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){updateRate();}public void afterTextChanged(Editable e){}});updateRate();
  space(settings,14);Button save=button("Сохранить",0xff28384d,FG);full(settings,save);save.setOnClickListener(v->{if(saveInterval())Toast.makeText(this,"Сохранено",Toast.LENGTH_SHORT).show();});space(root,16);
  show=button("Показать панель  ›",MINT,INK);full(root,show);show.setOnClickListener(v->{if(saveInterval()){if(ClickService.instance!=null){ClickService.instance.showOverlay();moveTaskToBack(true);}else Toast.makeText(this,"Включите службу AutoClick X",Toast.LENGTH_LONG).show();}});
  space(root,18);LinearLayout help=card(root);help.addView(text("Быстрый старт",18,FG,true));space(help,8);help.addView(text("01  Включи службу и выбери интервал.\n\n02  Покажи панель и перемести мишень.\n\n03  ▶ запускает касания, ■ останавливает, × скрывает панель. ⚙ открывает настройки, ⠿ перемещает панель.",14,MUTED,false));
  root.setFocusableInTouchMode(true);root.requestFocus();
 }
 @Override protected void onResume(){super.onResume();if(interval!=null&&!interval.hasFocus())interval.setText(String.valueOf(getSharedPreferences("settings",MODE_PRIVATE).getInt("interval",500)));if(status!=null){boolean ready=ClickService.instance!=null;status.setText(ready?"●  Служба подключена":"○  Служба отключена");status.setTextColor(ready?MINT:0xfffbbf24);}}
 private void updateRate(){try{int n=Integer.parseInt(interval.getText().toString());if(n<1||n>60000)throw new NumberFormatException();rate.setText("Диапазон 1–60 000 мс. Реальная скорость зависит от Android и устройства.");}catch(NumberFormatException e){rate.setText("Введите интервал от 1 до 60 000 мс.");}}
 private boolean saveInterval(){try{int ms=Integer.parseInt(interval.getText().toString().trim());if(ms<1||ms>60000)throw new NumberFormatException();getSharedPreferences("settings",MODE_PRIVATE).edit().putInt("interval",ms).apply();return true;}catch(NumberFormatException e){interval.setError("Введите число от 1 до 60 000");return false;}}
}
