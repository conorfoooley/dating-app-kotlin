package com.mmdev.roove.ui.main.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.mmdev.business.user.model.UserItem
import com.mmdev.roove.R
import com.mmdev.roove.core.GlideApp
import com.mmdev.roove.core.injector
import com.mmdev.roove.ui.activities.ActivitiesFragment
import com.mmdev.roove.ui.activities.conversations.view.ConversationsFragment
import com.mmdev.roove.ui.auth.view.AuthActivity
import com.mmdev.roove.ui.auth.viewmodel.AuthViewModel
import com.mmdev.roove.ui.cards.view.CardsFragment
import com.mmdev.roove.ui.chat.view.ChatFragment
import com.mmdev.roove.ui.custom.CustomAlertDialog
import com.mmdev.roove.ui.custom.LoadingDialog
import com.mmdev.roove.ui.feed.FeedFragment
import com.mmdev.roove.ui.main.viewmodel.local.LocalUserRepoVM
import com.mmdev.roove.ui.profile.view.ProfileFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class MainActivity: AppCompatActivity(R.layout.activity_main),
                    MainActivityListeners {

	companion object{
		private const val TAG = "myLogs"
	}

	lateinit var progressDialog: LoadingDialog

	private lateinit var drawerLayout: DrawerLayout
	private lateinit var toggle: ActionBarDrawerToggle
	lateinit var toolbar: Toolbar
	lateinit var appbar: AppBarLayout
	private lateinit var params: AppBarLayout.LayoutParams

	private lateinit var ivSignedInUserAvatar: ImageView
	private lateinit var tvSignedInUserName: TextView

	lateinit var userItemModel: UserItem
	private lateinit var mFragmentManager: FragmentManager

	private lateinit var authViewModel: AuthViewModel
	private val authViewModelFactory = injector.authViewModelFactory()
	private val mainViewModelFactory = injector.localUserRepoVMFactory()
	private val disposables = CompositeDisposable()


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		FirebaseAnalytics.getInstance(this@MainActivity)
		authViewModel = ViewModelProvider(this@MainActivity, authViewModelFactory)
			.get(AuthViewModel::class.java)

		userItemModel = ViewModelProvider(this@MainActivity, mainViewModelFactory)
			.get(LocalUserRepoVM::class.java)
			.getSavedUser()

		drawerLayout = findViewById(R.id.drawer_layout)
		appbar = findViewById(R.id.app_bar)
		toolbar = findViewById(R.id.toolbar)
		setSupportActionBar(toolbar)

		params = toolbar.layoutParams as AppBarLayout.LayoutParams
		setToolbarNavigation()
		setNavigationView()

		mFragmentManager = supportFragmentManager
		showFeedFragment()

		progressDialog = LoadingDialog(this@MainActivity)


	}

	// show main feed fragment
	private fun showFeedFragment(){
		if (mFragmentManager.findFragmentByTag(FeedFragment::class.java.canonicalName) == null)
			mFragmentManager.beginTransaction().apply {
				add(R.id.main_container,
				    FeedFragment.newInstance(),
				    FeedFragment::class.java.canonicalName)
				commit()
			}
		else mFragmentManager.popBackStack(null,
		                                   FragmentManager.POP_BACK_STACK_INCLUSIVE)



	}

	override fun onCardsClick() = startCardFragment()
	//todo: change this to messages fragment
	override fun onMessagesClick() = startConversationsFragment()

	override fun onLogOutClick() = showSignOutPrompt()


	private fun startActivitiesFragment(){
		mFragmentManager.findFragmentByTag(ActivitiesFragment::class.java.canonicalName) ?:
		mFragmentManager.beginTransaction().apply {
			setCustomAnimations(R.anim.enter_from_right,
			                    R.anim.exit_to_left,
			                    R.anim.enter_from_left,
			                    R.anim.exit_to_right)
			replace(R.id.main_container,
			        ActivitiesFragment.newInstance(),
			        ActivitiesFragment::class.java.canonicalName)
			addToBackStack(null)
			commit()
		}
	}

	/*
	 * start card swipe
	 */
	private fun startCardFragment() {
		mFragmentManager.findFragmentByTag(CardsFragment::class.java.canonicalName) ?:
		mFragmentManager.beginTransaction().apply {
			setCustomAnimations(R.anim.enter_from_right,
			                    R.anim.exit_to_left,
			                    R.anim.enter_from_left,
			                    R.anim.exit_to_right)
			replace(R.id.main_container,
			    CardsFragment.newInstance(),
			    CardsFragment::class.java.canonicalName)
			addToBackStack(CardsFragment::class.java.canonicalName)
			commit()
		}
	}

	private fun replaceFragment (fragment: Fragment) {
		val backStateName = fragment.javaClass.name
		val fragmentPopped = mFragmentManager.popBackStackImmediate(backStateName, 0)

		if (!fragmentPopped){ //fragment not in back stack, create it.
			mFragmentManager.beginTransaction().apply {
				setCustomAnimations(R.anim.enter_from_right,
				                    R.anim.exit_to_left,
				                    R.anim.enter_from_left,
				                    R.anim.exit_to_right)
				replace(R.id.main_container, fragment, fragment.javaClass.name)
				addToBackStack(backStateName)
				commit()
			}
		}
	}


	private fun startConversationsFragment(){
		mFragmentManager.findFragmentByTag(ConversationsFragment::class.java.canonicalName) ?:
		mFragmentManager.beginTransaction().apply {
			setCustomAnimations(R.anim.enter_from_right,
			                    R.anim.exit_to_left,
			                    R.anim.enter_from_left,
			                    R.anim.exit_to_right)
			replace(R.id.main_container,
			        ConversationsFragment.newInstance(),
			        ConversationsFragment::class.java.canonicalName)
			addToBackStack(null)
			commit()
		}
	}
	/*
	 * start chat
	 */
	fun startChatFragment(conversationId: String) {
		mFragmentManager.findFragmentByTag(ChatFragment::class.java.canonicalName) ?:
		mFragmentManager.beginTransaction().apply {
			setCustomAnimations(R.anim.enter_from_right,
			                    R.anim.exit_to_left,
			                    R.anim.enter_from_left,
			                    R.anim.exit_to_right)
			replace(R.id.main_container,
			        ChatFragment.newInstance(conversationId),
			        ChatFragment::class.java.canonicalName)
			addToBackStack(null)
			commit()
		}
	}

	fun startProfileFragment(userId: String) {
		mFragmentManager.findFragmentByTag(ProfileFragment::class.java.canonicalName) ?:
		mFragmentManager.beginTransaction().apply {
			setCustomAnimations(R.anim.enter_from_bottom,
			                    R.anim.exit_to_top,
			                    R.anim.enter_from_top,
			                    R.anim.exit_to_bottom)
			replace(R.id.main_core_container,
			        ProfileFragment.newInstance(userId),
			        ProfileFragment::class.java.canonicalName)
			addToBackStack(null)
			commit()
		}
	}

	override fun startAuthActivity(){
		val authIntent = Intent(this@MainActivity, AuthActivity::class.java)
		startActivity(authIntent)
		finish()
	}

	/*
	* log out pop up
	*/
	private fun showSignOutPrompt() {
		val builder = CustomAlertDialog.Builder(this@MainActivity)
		builder.setMessage("Do you wish to sign out?")
		builder.setPositiveBtnText("Yes")
		builder.setNegativeBtnText("NO")
		builder.OnPositiveClicked(View.OnClickListener {
			authViewModel.logOut()
		})

		builder.build()

	}

	/*
	 * adds an background thread that listens user auth status
	 */
	private fun checkConnection() {
		disposables.add(authViewModel.isAuthenticated()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ if (it == false) {
	            Log.wtf(TAG, "USER LOGGED OUT")
	            startAuthActivity()
            }
            else Log.wtf(TAG, "user is auth") },
                   {
                       Log.wtf(TAG, it)
                   }))
	}


	private fun setUpUser() {
		tvSignedInUserName.text = userItemModel.name
		GlideApp.with(this@MainActivity)
			.load(userItemModel.mainPhotoUrl)
			.apply(RequestOptions().circleCrop())
			.into(ivSignedInUserAvatar)

	}

	private fun setToolbarNavigation(){
		toggle = ActionBarDrawerToggle(this@MainActivity, drawerLayout, toolbar,
		                                   R.string.navigation_drawer_open,
		                                   R.string.navigation_drawer_close)

		drawerLayout.addDrawerListener(toggle)
		toggle.syncState()
	}

	private fun setNavigationView() {
		val navView: NavigationView = findViewById(R.id.nav_view)
		navView.getChildAt(navView.childCount - 1).overScrollMode = View.OVER_SCROLL_NEVER
		val headerView = navView.getHeaderView(0)
		tvSignedInUserName = headerView.findViewById(R.id.signed_in_username_tv)
		ivSignedInUserAvatar = headerView.findViewById(R.id.signed_in_user_image_view)
		setUpUser()
		navView.setNavigationItemSelectedListener { item ->
			drawerLayout.closeDrawer(GravityCompat.START)
			// Handle navigation view item clicks here.
			when (item.itemId) {
				R.id.nav_feed -> { showFeedFragment() }
				R.id.nav_cards -> onCardsClick()
				R.id.nav_activities -> startActivitiesFragment()
				R.id.nav_notifications -> { progressDialog.showDialog()
					Handler().postDelayed({ progressDialog.dismissDialog() }, 5000) }
				R.id.nav_account -> { }
				R.id.nav_log_out -> onLogOutClick()
			}
			return@setNavigationItemSelectedListener true
		}

	}

	fun setNonScrollableToolbar(){
		params.scrollFlags = 0
		toolbar.layoutParams = params

	}

	fun setScrollableToolbar(){
		params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
				AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
				AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
		toolbar.layoutParams = params
	}


	/*
	 * generate random users to firestore
	 */
//	private fun onGenerateUsers() {
//		usersCards.clear()
//		val usersCollection = mFirestore!!.collection("users")
//		usersCards.addAll(FeedManager.generateUsers())
//		for (i in usersCards) usersCollection.document(i.userId).set(i)
//
//
//		/*
//            generate likes/matches/skips lists
//             */
//		        profiles.document(mFirebaseAuth.getCurrentUser().getUid())
//		                .collection("likes")
//		                .document(mFirebaseAuth.getCurrentUser().getUid() + usersCards.get(1).getName())
//		                .set(usersCards.get(1));
//		        profiles.document(mFirebaseAuth.getCurrentUser().getUid())
//		                .collection("matches")
//		                .document(mFirebaseAuth.getCurrentUser().getUid() + usersCards.get(2).getName())
//		                .set(usersCards.get(2));
//		        profiles.document(mFirebaseAuth.getCurrentUser().getUid())
//		                .collection("skips")
//		                .document(mFirebaseAuth.getCurrentUser().getUid() + usersCards.get(3).getName())
//		                .set(usersCards.get(3));
//
//		        profiles.get().addOnCompleteListener(task -> {
//		            String a;
//		            if (task.isSuccessful())
//		            {
//		                a = task.getResult().getDocuments().get(0).get("Name").toString();
//		                new Handler().postDelayed(() -> Toast.makeText(getApplicationContext(), "Name : " + String.valueOf(a), Toast.LENGTH_SHORT).show(), 1000);
//		            }
//		        });
//
//	}

	fun showInternetError(error: String) {
		Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
	}

	override fun onBackPressed() {
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
		else super.onBackPressed()
	}

	override fun onDestroy() {
		super.onDestroy()
		//disposables.dispose()
		disposables.clear()
	}

	override fun onStart() {
		super.onStart()
		checkConnection()
	}


}
