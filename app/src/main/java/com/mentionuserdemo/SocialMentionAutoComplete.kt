package com.mentionuserdemo

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.ArrayMap
import android.util.AttributeSet
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import androidx.core.content.res.ResourcesCompat
import java.util.regex.Pattern

class SocialMentionAutoComplete : AppCompatMultiAutoCompleteTextView {
    var map = ArrayMap<String, String>()
    var formattedOfString = "@%s "

    interface textShows {
        fun autoText(query: String)
    }

    companion object {
        var textSHowsInter: textShows? = null
    }

    var mentionAutoCompleteAdapter: MentionAutoCompleteAdapter? = null

    var currentQuery = ""

    private val users = arrayOf("meet kachhadiya", "praveen yadav", "john_doe", "alice")

    constructor(context: Context?) : super(context!!) {
        initializeComponents()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        initializeComponents()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        initializeComponents()
    }

    private fun initializeComponents() {
        addTextChangedListener(textWatcher)
        onItemClickListener = onItemSelectedListener
        setTokenizer(SpaceTokenizer())
    }

    var onItemSelectedListener = OnItemClickListener { adapterView, view, i, l ->
        val mentionPerson = users[i]
        map["@" + mentionPerson] = mentionPerson
    }


    var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            lengthBefore: Int,
            lengthAfter: Int
        ) {
            if (!s.toString().isEmpty() && start < s.length) {
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
                    name = s.toString().substring(lastTokenIndex, tokenEnd).trim { it <= ' ' }
                    val pattern = Pattern.compile("^(.+)\\s.+")
                    val matcher = pattern.matcher(name)
                    if (!matcher.find()) {
                        name = name.replace("@", "").trim { it <= ' ' }
                        if (!name.isEmpty()) {
                            getUsers(name)
                            currentQuery = name
                            textSHowsInter?.autoText(name)
                        }
                    }
                }
            }
        }

        override fun afterTextChanged(editable: Editable) {}
    }

    /*
     *This function returns results from the web server according to the user name
     * I have used Retrofit for Api Communications
     * */
    fun getUsers(name: String?) {
        val mentionPeople: MutableList<String> = ArrayList<String>()
        mentionPeople.add("Meet Kachhadiya")
        mentionPeople.add("Praveen yadav")
        mentionPeople.add("valentine")
        mentionPeople.add("vishal thakur")

//        val filteredUsers = users.filter { it.contains(name, ignoreCase = true) }
//        val adapter = ArrayAdapter(this@SocialMentionAutoComplete, android.R.layout.simple_dropdown_item_1line, filteredUsers)
//        autoCompleteTextView.setAdapter(adapter)
//        if (filteredUsers.isNotEmpty()) {
//            autoCompleteTextView.showDropDown()
//        } else {
//            autoCompleteTextView.dismissDropDown()
//        }

//        mentionAutoCompleteAdapter = MentionAutoCompleteAdapter(context, mentionPeople)
//        setAdapter(mentionAutoCompleteAdapter)
//        showDropDown()


    }

    inner class SpaceTokenizer : Tokenizer {
        override fun findTokenStart(text: CharSequence, cursor: Int): Int {
            var i = cursor
            while (i > 0 && text[i - 1] != ' ') {
                i--
            }
            while (i < cursor && text[i] == ' ') {
                i++
            }
            return i
        }

        override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
            var i = cursor
            val len = text.length
            while (i < len) {
                if (text[i] == ' ') {
                    return i
                } else {
                    i++
                }
            }
            return len
        }

        override fun terminateToken(text: CharSequence): CharSequence {
            var i = text.length
            while (i > 0 && text[i - 1] == ' ') {
                i--
            }
            return if (i > 0 && text[i - 1] == ' ') {
                text
            } else {
                // Returns colored text for selected token
                val sp = SpannableString(String.format(formattedOfString, text))
                val textColor: Int = ResourcesCompat.getColor(resources, R.color.purple_200, null)
                sp.setSpan(
                    ForegroundColorSpan(textColor),
                    0,
                    text.length + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                sp
            }
        }
    }
}