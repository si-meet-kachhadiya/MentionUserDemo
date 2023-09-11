package com.mentionuserdemo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mentionuserdemo.databinding.ActivityMainBinding
import java.util.regex.Pattern

class MainActivity : AppCompatActivity(), SocialMentionAutoComplete.textShows {

    private val users = arrayOf("meet kachhadiya", "praveen yadav", "john_doe", "alice")

    val mb by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mb.root)
        initialize()

        SocialMentionAutoComplete.textSHowsInter = this@MainActivity
    }

    private fun initialize() {
        mb.apply {

            val adapter =
                ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, users)
            autoCompleteTextView.setAdapter(adapter)

            autoCompleteTextView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    val selectedUser = autoCompleteTextView.text.toString()
                    val currentText = editText.text.toString()
                    val mentionText = "@$selectedUser "
                    val newText =
                        currentText.replaceRange(
                            editText.selectionStart,
                            editText.selectionEnd,
                            mentionText
                        )
                    editText.setText(newText)
                    autoCompleteTextView.text = null
                }

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (!s.toString().isEmpty() && start < s!!.length) {
                        var name = s.toString().substring(0, start + 1)
                        var lastTokenIndex = name.lastIndexOf(" @")
                        val lastIndexOfSpace = name.lastIndexOf(" ")
                        val nextIndexOfSpace = name.indexOf(" ", start)
                        if (lastIndexOfSpace > 0 && lastTokenIndex < lastIndexOfSpace) {
                            val afterString = s.toString().substring(lastIndexOfSpace, s.length)
                            if (afterString.startsWith(" ")) return
                        }

                        if (lastTokenIndex < 0) {
                            lastTokenIndex =
                                if (!name.isEmpty() && name.length >= 1 && name.startsWith("@")) {
                                    1
                                } else return

                        }
                        var tokenEnd = lastIndexOfSpace
                        if (lastIndexOfSpace <= lastTokenIndex) {
                            tokenEnd = name.length
                            if (nextIndexOfSpace != -1 && nextIndexOfSpace < tokenEnd) {
                                tokenEnd = nextIndexOfSpace
                            }
                        }

                        if (lastTokenIndex >= 0) {
                            autoCompleteTextView.showDropDown()
                            name =
                                s.toString().substring(lastTokenIndex, tokenEnd).trim { it <= ' ' }
                            val pattern = Pattern.compile("^(.+)\\s.+")
                            val matcher = pattern.matcher(name)
                            if (!matcher.find()) {
                                name = name.replace("@", "").trim { it <= ' ' }
                                if (!name.isEmpty()) {
                                    filterUsers(name)
                                }
                            }
                        }
                    }

                    /*if (!s.isNullOrEmpty() && s[start] == '@') {
                        val query = s.substring(start + 1)
                        filterUsers(query)
                    } else {
                        autoCompleteTextView.dismissDropDown()
                    }*/
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun filterUsers(query: String) {
        mb.apply {
            val filteredUsers = users.filter { it.contains(query, ignoreCase = true) }
            val adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_dropdown_item_1line,
                filteredUsers
            )
            autoCompleteTextView.setAdapter(adapter)
            if (filteredUsers.isNotEmpty()) {
                autoCompleteTextView.showDropDown()
            } else {
                autoCompleteTextView.dismissDropDown()
            }
        }
    }

    override fun autoText(query: String) {
        filterUsers(query)
        map["@" + mentionPerson] = mentionPerson
    }
}

