package com.jikexueyuan.game2048;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private TextView tvScore;
	private static MainActivity mainActivity = null;
	private int score = 0;

	public MainActivity(){
		mainActivity = this;
	}
	
	public static MainActivity getMainActivity() {
		return mainActivity;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvScore = (TextView) findViewById(R.id.tvScore);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void clearScore() {
		score = 0;
		showScore();
	}
    
    public void addScore(int s){
    	score+=s;
    	showScore();
    }
    
    public void showScore(){
    	tvScore.setText(" : "+score+"");
    }
    
    
}




