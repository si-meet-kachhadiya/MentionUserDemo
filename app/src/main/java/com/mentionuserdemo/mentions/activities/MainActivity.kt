package com.mentionuserdemo.mentions.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mentionuserdemo.R
import com.mentionuserdemo.mentions.Mentions
import com.mentionuserdemo.mentions.QueryListener
import com.mentionuserdemo.mentions.SuggestionsListener
import com.mentionuserdemo.mentions.adapters.CommentsAdapter
import com.mentionuserdemo.mentions.adapters.RecyclerItemClickListener
import com.mentionuserdemo.mentions.adapters.UsersAdapter
import com.mentionuserdemo.mentions.models.Comment
import com.mentionuserdemo.mentions.models.Mention
import com.mentionuserdemo.mentions.utils.MentionsLoaderUtils
import com.percolate.caffeine.ViewUtils
import org.apache.commons.lang3.StringUtils

/**
 * Sample App which demonstrates how to use the Mentions library. A comment box is displayed at
 * the bottom which allows you to '@' mention a user. Click on the send button will display the
 * test comment above the comment box. All the mentions you choose will be highlighted.
 */
class MainActivity : AppCompatActivity(), QueryListener, SuggestionsListener {
    /**
     * Comment field.
     */
    private var commentField: EditText? = null

    /**
     * Send button.
     */
    private var sendCommentButton: Button? = null

    /**
     * Adapter to display suggestions.
     */
    private var usersAdapter: UsersAdapter? = null

    /**
     * Adapter to display comments.
     */
    private var commentsAdapter: CommentsAdapter? = null

    /**
     * Utility class to load from a JSON file.
     */
    private var mentionsLoaderUtils: MentionsLoaderUtils? = null

    /**
     * Mention object provided by library to configure at mentions.
     */
    private var mentions: Mentions? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_t)
        init()
        setupMentionsList()
        setupCommentsList()
        setupSendButtonTextWatcher()
    }

    /**
     * Initialize views and utility objects.
     */
    private fun init() {
        commentField = ViewUtils.findViewById(this, R.id.comment_field)
        sendCommentButton = ViewUtils.findViewById(this, R.id.send_comment)
        mentions = Mentions.Builder(this, commentField)
            .suggestionsListener(this)
            .queryListener(this)
            .build()
        mentionsLoaderUtils = MentionsLoaderUtils(this)
    }

    /**
     * Setups the mentions suggestions list. Creates and sets and adapter for
     * the mentions list and sets the on item click listener.
     */
    private fun setupMentionsList() {
        val mentionsList = ViewUtils.findViewById<RecyclerView>(this, R.id.mentions_list)
        mentionsList.layoutManager = LinearLayoutManager(this)
        usersAdapter = UsersAdapter(this)
        mentionsList.adapter = usersAdapter

        // set on item click listener
        mentionsList.addOnItemTouchListener(RecyclerItemClickListener(this) { view, position ->
            val user = usersAdapter!!.getItem(position)
            /*
                     * We are creating a mentions object which implements the
                     * <code>Mentionable</code> interface this allows the library to set the offset
                     * and length of the mention.
                     */if (user != null) {
            val mention = Mention()
            mention.mentionName = user.fullName
            mentions!!.insertMention(mention)
        }
        })
    }

    /**
     * After typing some text with mentions in the comment box and clicking on send, you will
     * be able to see the comment with the mentions highlighted above the comment box. This method
     * setups the adapter for this list.
     */
    private fun setupCommentsList() {
        val commentsList = ViewUtils.findViewById<RecyclerView>(this, R.id.comments_list)
        commentsList.layoutManager = LinearLayoutManager(this)
        commentsAdapter = CommentsAdapter(this)
        commentsList.adapter = commentsAdapter

        // setup send button
        sendCommentButton!!.setOnClickListener {
            if (StringUtils.isNotBlank(commentField!!.text)) {
                ViewUtils.hideView(this@MainActivity, R.id.comments_empty_view)

                // add comment to list
                val comment = Comment()
                comment.comment = commentField!!.text.toString()
                comment.mentions = mentions!!.insertedMentions
                commentsAdapter!!.add(comment)


                var startBraced = "/{"
                var endBraced = "} "

                var formatedString = comment.comment
                var newString = ""
                for (i in comment.mentions.indices){
                    val start = mentions?.insertedMentions?.get(i)?.mentionOffset
                    val end = start?.plus(mentions?.insertedMentions?.get(i)?.mentionLength!!)!!

                    if (comment.mentions.lastIndex == i) {
                        newString = newString + formatedString.substring(
                            newString.length-2,
                            start
                        ) + startBraced + formatedString.substring(start, end) + endBraced + formatedString.substring(end)
                    } else {
                        newString = newString + formatedString.substring(
                            newString.length,
                            start
                        ) + startBraced + formatedString.substring(start, end) + endBraced
                    }

                }



                Log.e("TAG", "onClick: fs-->" +formatedString)
                Log.e("TAG", "onClick: ns-->" +newString)

                // clear comment field
                commentField!!.setText("")
            }
        }
    }

    /**
     * You could add your own text watcher to the edit text view in addition to the text
     * watcher the mentions library sets. This text watcher will toggle the text color
     * of the send button depending on whether something has been typed into the edit text
     * view.
     */
    private fun setupSendButtonTextWatcher() {
        val orange = ContextCompat.getColor(this@MainActivity, R.color.mentions_default_color)
        val orangeFaded = ContextCompat.getColor(this@MainActivity, R.color.orange_faded)
        commentField!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 0) {
                    sendCommentButton!!.setTextColor(orange)
                } else {
                    sendCommentButton!!.setTextColor(orangeFaded)
                }
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })
    }

    override fun onQueryReceived(query: String) {
        val users = mentionsLoaderUtils!!.searchUsers(query)
        if (users != null && !users.isEmpty()) {
            usersAdapter!!.clear()
            usersAdapter!!.setCurrentQuery(query)
            usersAdapter!!.addAll(users)
            showMentionsList(true)
        } else {
            showMentionsList(false)
        }
    }

    /**
     * If a user didn't enter a valid query, then this method will be called by the library to
     * notify you that the mentions list should be hidden. This method will also be called if
     * you insert a mention into the EditText view.
     *
     * @param display  boolean  true is the mentions list layout showed be shown or false
     * otherwise.
     */
    override fun displaySuggestions(display: Boolean) {
        if (display) {
            ViewUtils.showView(this, R.id.mentions_list_layout)
        } else {
            ViewUtils.hideView(this, R.id.mentions_list_layout)
        }
    }

    /**
     * Toggle the mentions list's visibility if there are search results returned for search
     * query. Shows the empty list view
     *
     * @param display boolean   true if the mentions list should be shown or false if
     * the empty suggestions list view should be shown.
     */
    private fun showMentionsList(display: Boolean) {
        ViewUtils.showView(this, R.id.mentions_list_layout)
        if (display) {
            ViewUtils.showView(this, R.id.mentions_list)
            ViewUtils.hideView(this, R.id.mentions_empty_view)
        } else {
            ViewUtils.hideView(this, R.id.mentions_list)
            ViewUtils.showView(this, R.id.mentions_empty_view)
        }
    }
}