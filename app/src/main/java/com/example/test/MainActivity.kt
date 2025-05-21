package com.example.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ログ出力テスト
        Log.d("MainActivity", "アプリが起動しました")

        // 文字列変数を作ってログに出力してみる
        val message = "GitHubテスト中"
        Log.d("MainActivity", "メッセージ: $message")

        // 数値計算のテスト（コミット用）
        val a = 5
        val b = 3
        val result = a + b
        Log.d("MainActivity", "計算結果: $a + $b = $result")
    }
}
