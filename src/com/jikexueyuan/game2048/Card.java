package com.jikexueyuan.game2048;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Card extends FrameLayout {

	private int num = 0;
	private TextView label;     //卡片需要呈现文字
	
	public Card(Context context) {
		super(context);
		
		label = new TextView(getContext());   //初始化
		label.setTextSize(32);    //设置文本大小
		label.setBackgroundColor(0x33ffffff);     //设置文字背景或颜色
		label.setGravity(Gravity.CENTER);   //居中文字显示     否则数字都在卡片的左上角

		LayoutParams lp = new LayoutParams(-1, -1);   //设置布局参数 填充满整个父级容器
		lp.setMargins(10, 10, 0, 0);     //设置文字间的间隔   用以区分各个card
		
		addView(label, lp);
		
		setNum(0);    //默认情况下卡片数字为0  !!!!!!!!!顺序不能错  否则会出bug
	} 
	
	public int getNum() {
		return num;
	}
	
	public void setNum(int num) {
		this.num = num;
		
		if(num <= 0){
			label.setText("");         //如果卡片中的数字是0，则不显示
		}else {
			label.setText(num+"");     //如果卡片中的数字不是0，则显示，此时num(int)会变成一个字符串
		}
	}
	
	public boolean equals(Card card) {             //判断两个卡片上的数字是否相同
		return this.getNum() == card.getNum();
	}

}
