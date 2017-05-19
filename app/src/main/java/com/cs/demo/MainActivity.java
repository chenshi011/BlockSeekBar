package com.cs.demo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cs.seekbar.BlockSeekBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BlockSeekBar seekBar =  (BlockSeekBar)findViewById(R.id.bsb1);
        seekBar.setOnBlockSeekBarChangedListener(new BlockSeekBar.OnBlockSeekBarChangedListener() {
            @Override
            public void onProgressChanged(BlockSeekBar blockSeekBar, int progress) {
                Toast.makeText(MainActivity.this,"blockSeekBar on progress:" + progress + " block:" + blockSeekBar.getCurBlock(),Toast.LENGTH_SHORT).show();
            }
        });

        BlockSeekBar seekBar2 =  (BlockSeekBar)findViewById(R.id.bsb2);
        seekBar2.setOnBlockSeekBarChangedListener(new BlockSeekBar.OnBlockSeekBarChangedListener() {
            @Override
            public void onProgressChanged(BlockSeekBar blockSeekBar, int progress) {
                Toast.makeText(MainActivity.this,"blockSeekBar on progress:" + progress + " block:" + blockSeekBar.getCurBlock(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
