package ca.ualberta.cs.cmput301t03app.views;

import java.util.ArrayList;

import ca.ualberta.cs.cmput301t03app.R;
import ca.ualberta.cs.cmput301t03app.controllers.GeoLocationTracker;
import ca.ualberta.cs.cmput301t03app.controllers.PostController;
import ca.ualberta.cs.cmput301t03app.controllers.PushController;
import ca.ualberta.cs.cmput301t03app.datamanagers.ServerDataManager;
import ca.ualberta.cs.cmput301t03app.models.Comment;
import ca.ualberta.cs.cmput301t03app.models.GeoLocation;
import ca.ualberta.cs.cmput301t03app.models.Question;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 *         ViewComment activity is the activity responsible for showing comments
 *         to either a given question or a given answer.
 * 
 */

public class ViewComment extends Activity
{
	private static ServerDataManager sdm = new ServerDataManager();
	private PushController pushCtrl = new PushController(this);
	private PostController pc = new PostController(this);
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	private ArrayList<String> commentBodyList = new ArrayList<String>();
	
	protected boolean hasLocation = false;	
	protected GeoLocation geoLocation;
	protected String cityName;
	private int commentType;
	private String questionID;
	private String answerID;
	private ArrayAdapter<String> cla;
	
	//UI objects
	ListView commentListView;
	Button commentButton;
	TextView commentCount;
	TextView timeStamp;
	TextView author;
	TextView location;
	AlertDialog dialog;
	

	/**
	 * Called when the activity is first created.
	 * 
	 * Determines through the intent whether the view was called through a
	 * question or an answer, and then populates the view accordingly.
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_comment);
		
		/* Removes the actionbar title text */
		getActionBar().setDisplayShowTitleEnabled(false);
		
		// enables the activity icon as a 'home' button. required if
		// "android:targetSdkVersion" > 14
		getActionBar().setHomeButtonEnabled(true);
		
		Bundle extras = getIntent().getExtras();
		commentType = extras.getInt(ViewQuestion.SET_COMMENT_TYPE); // answer or
																	// question
																	// comments
		Log.d("click", "Comment type: " + commentType);
		questionID = extras.getString(ViewQuestion.QUESTION_ID_KEY);
		switch (commentType)
		{
			case 1:
				comments = pc.getCommentsToQuestion(questionID);
				break;
			case 2:
				answerID = extras.getString(ViewQuestion.ANSWER_ID_KEY);
				comments = pc.getCommentsToAnswer(questionID, answerID);
				break;
		}
		instantiateViews();
		setListeners();
		setPostDetails();
		setCommentAdapter();
		updateCommentCount();
	}

	/**
	 * This method instantiates all the view objects.
	 */
	public void instantiateViews()
	{


		commentButton = (Button) findViewById(R.id.comment_button);
		commentCount = (TextView) findViewById(R.id.comment_count);
		timeStamp = (TextView) findViewById(R.id.comment_post_timestamp);
		author = (TextView) findViewById(R.id.comment_post_author);
		commentListView = (ListView) findViewById(R.id.commentListView);
		cla = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, commentBodyList);
		location = (TextView) findViewById(R.id.comment_location);
	}

	/**
	 * Creates the listener for the comment button.
	 */
	public void setListeners()
	{

		commentButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				addComment();
			}
		});
	}

	/**
	 *  Sets the question body,author and date from the question clicked from the
	 *  ViewQuestions activity. If an answer was clicked it also sets the answers
	 *  body.
	 */
	public void setPostDetails()
	{

		TextView commentTitle = (TextView) findViewById(R.id.comment_title);
		if (commentType == 1)
		{ // comment for questions
			commentTitle.setText("Q: "
					+ pc.getQuestion(questionID).getSubject());
			timeStamp
					.setText("Posted: " + pc.getQuestion(questionID).getDate());
			author.setText("By: " + pc.getQuestion(questionID).getAuthor());
			
			//Set location
			if (pc.getQuestion(questionID).getGeoLocation() != null) {
				location.setText("Location: " + pc.getQuestion(questionID).getGeoLocation().getCityName());
			}
			
		} else if (commentType == 2)
		{ // comment for answers
			commentTitle.setText("Q: "
					+ pc.getQuestion(questionID).getSubject()
					+ System.getProperty("line.separator") + "A: "
					+ pc.getAnswer(answerID, questionID).getAnswer());
			timeStamp.setText("Posted: "
					+ pc.getAnswer(answerID, questionID).getDate());
			author.setText("By: "
					+ pc.getAnswer(answerID, questionID).getAuthor());
			//Set location
			if (pc.getAnswer(answerID, questionID).getGeoLocation() != null) {
				location.setText("Location: " + pc.getAnswer(answerID, questionID).getGeoLocation().getCityName());
			}
		}
	}
	
	/**
	 *  This sets up the comment adapter by grabbing the comments
	 *  from the question or answer objects and feeding them to
	 *  the adapter.
	 */

	public void setCommentAdapter()
	{

		commentListView.setAdapter(cla);
		getCommentBodiesFromComment();
		cla.notifyDataSetChanged();
	}
	
	/**
	 * This extracts the comment bodies from the
	 * Comment object to be able to place them in the
	 * adapter.
	 */
	public void getCommentBodiesFromComment()
	{ // used for showing in the view

		if (comments != null)
		{
			commentBodyList.clear();
			for (int i = 0; i < comments.size(); i++)
				commentBodyList.add(comments.get(i).getCommentBody());
		}
	}

	
	/**
	 *  This updates the comment count for the view for either
	 *  answer or question.
	 * 
	 */
	public void updateCommentCount()
	{

		if (commentType == 1)
		{
			commentCount
					.setText("Comments: "
							+ String.valueOf(pc.getQuestion(questionID)
									.countComments()));
		} else if (commentType == 2)
		{
			commentCount.setText("Comments: "
					+ String.valueOf(pc.getAnswer(answerID, questionID)
							.countAnswerComments()));
		}
	}

	/**
	 * onClick method for adding comments, prompts the user with a dialog box
	 * which takes in a username (userNameString) and comment
	 * (commentBodyString) and then adds that comment to the either a question
	 * or an answer depending on the context.
	 */

	public void addComment()
	{

		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.comment_dialog, null);
		final EditText postBody = (EditText) promptsView
				.findViewById(R.id.comment_body);
		final EditText userName = (EditText) promptsView
				.findViewById(R.id.UsernameRespondTextView);
		
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this); // Create
																				// a
																				// new
																				// AlertDialog
		alertDialogBuilder.setView(promptsView);
		alertDialogBuilder.setPositiveButton("Comment",
				new DialogInterface.OnClickListener()
				{

					// Building the dialog for adding
					@Override
					public void onClick(DialogInterface dialog, int which)
					{

						String commentBodyString = (String) postBody.getText()
								.toString();
						String userNameString = (String) userName.getText()
								.toString();
						Comment c = new Comment(commentBodyString,
								userNameString);
						if (commentType == 1)
						{
							pc.addCommentToQuestion(c, questionID);
							comments = pc.getCommentsToQuestion(questionID);
							if (pc.checkConnectivity()) {
								Thread thread = new AddCommentThread(c,
										questionID);
								thread.start();
							} else {
								pc.addPushAnsAndComm(questionID, null, c);
							}
						} else if (commentType == 2)
						{
							pc.addCommentToAnswer(c, questionID, answerID);
							comments = pc.getCommentsToAnswer(questionID,
									answerID);
							if (pc.checkConnectivity()) {
								Thread thread = new AddCommentThread(c, questionID, answerID);
								thread.start();
							} else {
								pc.addPushAnsAndComm(questionID, answerID, c);
							}
						}
						// setCommentAdapter();
						
						commentBodyList.add(commentBodyString);
						cla.notifyDataSetChanged();
						updateCommentCount(); // <-- MIGHT NOT USE THIS
					}

				}).setNegativeButton("Cancel",
				new DialogInterface.OnClickListener()
				{

					public void onClick(DialogInterface dialog, int id)
					{

						// Do nothing
						hasLocation = false;
						dialog.cancel();
					}
				});

		final AlertDialog alertDialog = alertDialogBuilder.create();
		dialog = alertDialog;
		alertDialog.show();
		alertDialog.getButton(AlertDialog.BUTTON1).setEnabled(false);

		// creating a listener to see if any changes to edit text in dialog
		TextWatcher textwatcher = new TextWatcher()
		{

			private void handleText()
			{

				final Button button = alertDialog
						.getButton(AlertDialog.BUTTON_POSITIVE);
				if (postBody.getText().length() == 0)
				{ // these checks the edittext to make sure not empty edit text
					button.setEnabled(false);
				} else if (userName.getText().length() == 0)
				{
					button.setEnabled(false);
				} else
				{
					button.setEnabled(true);
				}
			}

			@Override
			public void afterTextChanged(Editable s)
			{

				handleText();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after)
			{

				// do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count)
			{

				// do nothing
			}
		};
		postBody.addTextChangedListener(textwatcher); // adding listeners to the
														// edittexts
		userName.addTextChangedListener(textwatcher);
		Toast.makeText(this, "Please write your comment", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_question, menu);
		getActionBar().setHomeButtonEnabled(true);
		
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		
		return (super.onOptionsItemSelected(item));
	}
	
	/**
	 * Thread that pushes a Comment to the server.  The constructor is overloaded to allow for distinction between a Comment to a Question
	 * and a Comment to an Answer.
	 *
	 */
	class AddCommentThread extends Thread {
    	private String qID;
    	private String aID;
    	private Comment comment;
    	
    	public AddCommentThread(Comment comment, String qID) {
    		this.qID = qID;
    		this.aID = null;
    		this.comment = comment;
    		//Log.d("push", this.question.getSubject());
    	}
    	
    	public AddCommentThread(Comment comment, String qID, String aID) {
    		this.qID = qID;
    		this.aID = aID;
    		this.comment = comment;
    	}
    	
    	@Override
    	public void run() {
    		if (this.aID == null) {
    			pushCtrl.commentAQuestionToServer(this.comment, this.qID);
    		} else {
    			pushCtrl.commentAnAnswerToServer(this.comment, this.aID, this.qID);
    		}
    		try {
    			Thread.sleep(500);
    		} catch(InterruptedException e) {
    			e.printStackTrace();
    		}
    		
    	}
    }
	
	//Used for testing.
	public AlertDialog getDialog()
	{

		return dialog;
	}
}
