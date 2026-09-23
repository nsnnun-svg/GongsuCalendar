package com.gongsu.calendar.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object UiHelper {
    private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

    fun dp(ctx: Context, v: Int): Int =
        (v * ctx.resources.displayMetrics.density + 0.5f).toInt()

    /** 상태바/하단 버튼 영역과 겹치지 않도록 여백을 줌 */
    fun applyInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }
    }

    /** 다이얼로그 안 입력칸에 여백 주기 */
    fun padded(ctx: Context, child: View): FrameLayout {
        val f = FrameLayout(ctx)
        f.setPadding(dp(ctx, 20), dp(ctx, 12), dp(ctx, 20), dp(ctx, 4))
        f.addView(child, FrameLayout.LayoutParams(MATCH, WRAP))
        return f
    }

    fun header(ctx: Context, title: String, onBack: () -> Unit): TextView {
        val t = TextView(ctx)
        t.text = "←  $title"
        t.textSize = 20f
        t.setTextColor(Color.WHITE)
        t.setBackgroundColor(0xFF2E7D32.toInt())
        t.gravity = Gravity.CENTER_VERTICAL
        t.setPadding(dp(ctx, 16), 0, dp(ctx, 16), 0)
        t.layoutParams = LinearLayout.LayoutParams(MATCH, dp(ctx, 56))
        t.setOnClickListener { onBack() }
        return t
    }

    fun label(ctx: Context, text: String): TextView {
        val t = TextView(ctx)
        t.text = text
        t.textSize = 14f
        t.setTextColor(0xFF666666.toInt())
        t.setPadding(0, dp(ctx, 16), 0, dp(ctx, 4))
        return t
    }

    fun row(ctx: Context, label: String, value: String, bold: Boolean = false): LinearLayout {
        val r = LinearLayout(ctx)
        r.orientation = LinearLayout.HORIZONTAL
        r.setPadding(0, dp(ctx, 10), 0, dp(ctx, 10))

        val l = TextView(ctx)
        l.text = label
        l.textSize = 16f
        l.setTextColor(0xFF444444.toInt())
        l.layoutParams = LinearLayout.LayoutParams(0, WRAP, 1f)

        val v = TextView(ctx)
        v.text = value
        v.textSize = 16f
        v.gravity = Gravity.END
        v.setTextColor(0xFF111111.toInt())
        if (bold) v.typeface = Typeface.DEFAULT_BOLD
        v.layoutParams = LinearLayout.LayoutParams(0, WRAP, 1f)

        r.addView(l)
        r.addView(v)
        return r
    }

    fun divider(ctx: Context): View {
        val v = View(ctx)
        v.setBackgroundColor(0xFFE0E0E0.toInt())
        v.layoutParams = LinearLayout.LayoutParams(MATCH, dp(ctx, 1))
        return v
    }
}
