package com.autotapper.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText
    private lateinit var intervalInput: EditText
    private lateinit var radiusInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(Color.rgb(247, 249, 252))
        }
        fun label(text: String) {
            root.addView(TextView(this).apply {
                this.text = text
                textSize = 15f
                setTextColor(Color.DKGRAY)
                setPadding(0, 14, 0, 5)
            })
        }
        fun input(hint: String, value: String): EditText {
            return EditText(this).apply {
                this.hint = hint
                setSingleLine(true)
                setText(value)
                textSize = 16f
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(80, 110, 160))
                root.addView(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        root.addView(TextView(this).apply {
            text = "AutoTapper"
            textSize = 30f
            setTextColor(Color.rgb(25, 55, 95))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 12)
        })
        root.addView(TextView(this).apply {
            text = "نقر تلقائي بإحداثيات تختارها. فعّل خدمة إمكانية الوصول أولاً."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        })
        label("إحداثي X")
        xInput = input("مثال: 500", prefs.getInt("x", 500).toString())
        label("إحداثي Y")
        yInput = input("مثال: 900", prefs.getInt("y", 900).toString())
        label("الفاصل بين النقرات (مللي ثانية)")
        intervalInput = input("مثال: 1000", prefs.getLong("interval", 1000L).toString())
        label("نطاق تغيير مكان النقرة (بكسل)")
        radiusInput = input("مثال: 12", prefs.getInt("radius", 12).toString())

        fun button(text: String, action: () -> Unit) {
            root.addView(Button(this).apply {
                this.text = text
                textSize = 15f
                setOnClickListener { action() }
            }, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        button("1) تفعيل خدمة النقر") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        button("2) حفظ الإعدادات") {
            val x = xInput.text.toString().toIntOrNull()
            val y = yInput.text.toString().toIntOrNull()
            val interval = intervalInput.text.toString().toLongOrNull()
            val radius = radiusInput.text.toString().toIntOrNull()
            if (x == null || y == null || interval == null || radius == null ||
                x < 0 || y < 0 || interval < 250 || radius !in 0..300) {
                Toast.makeText(this, "تحقق من القيم. الفاصل 250 مللي ثانية على الأقل.", Toast.LENGTH_LONG).show()
                return@button
            }
            prefs.edit().putInt("x", x).putInt("y", y)
                .putLong("interval", interval).putInt("radius", radius).apply()
            Toast.makeText(this, "تم حفظ الإعدادات", Toast.LENGTH_SHORT).show()
        }
        button("بدء النقر") {
            saveCurrent()
            sendBroadcast(Intent(AutoClickService.ACTION_START).setPackage(packageName))
        }
        button("إيقاف النقر") {
            sendBroadcast(Intent(AutoClickService.ACTION_STOP).setPackage(packageName))
        }
        root.addView(TextView(this).apply {
            text = "تنبيه: هذا الإصدار ينقر بشكل دوري في الموقع المحدد ولا يتعرّف بصرياً على ظهور 1x داخل اللعبة. أوقفه قبل مغادرة التطبيق."
            textSize = 13f
            setTextColor(Color.rgb(150, 65, 35))
            setPadding(0, 18, 0, 0)
        })
        setContentView(root)
    }

    private fun saveCurrent() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val x = xInput.text.toString().toIntOrNull() ?: 500
        val y = yInput.text.toString().toIntOrNull() ?: 900
        val interval = (intervalInput.text.toString().toLongOrNull() ?: 1000L).coerceAtLeast(250L)
        val radius = (radiusInput.text.toString().toIntOrNull() ?: 12).coerceIn(0, 300)
        prefs.edit().putInt("x", x).putInt("y", y)
            .putLong("interval", interval).putInt("radius", radius).apply()
    }
}
