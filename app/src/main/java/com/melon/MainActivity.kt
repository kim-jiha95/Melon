package com.melon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //newInstance 하는 이유는 재생성 때문이다.(화면회전, 다크모드 등)
        //또한 안드로이드에서 메모리가 부족해지면 엑티비티뿐만아니라 프래그먼트도 파기된다. 재생성시 빈생성자가 있어야하며, 재생성시 받아온 데이터를 유지해야 한다.
        //이러한 이유들로 잦은 재생성이 일어나는 프래그먼트는 이에대한 해결방법으로 newInstance()메서드를 활용한다.(참조 : https://black-jin0427.tistory.com/250)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, PlayerFragment.newInstance())
                .commit()
    }
}