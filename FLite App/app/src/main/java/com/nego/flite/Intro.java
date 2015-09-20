package com.nego.flite;

import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.nego.flite.Slide.FirstSlide;
import com.nego.flite.Slide.FourthSlide;
import com.nego.flite.Slide.SecondSlide;
import com.nego.flite.Slide.ThirdSlide;


public class Intro extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(new FirstSlide(), getApplicationContext());
        addSlide(new SecondSlide(), getApplicationContext());
        addSlide(new ThirdSlide(), getApplicationContext());
        addSlide(new FourthSlide(), getApplicationContext());

    }

    @Override
    public void onDonePressed() {
        finish();
    }
}
