package com.mmdev.meetups.utils;

import com.mmdev.meetups.R;
import com.mmdev.meetups.models.FeedItem;
import com.mmdev.meetups.models.ProfileModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FeedManager
{

	private List<ProfileModel> usersCards;
	public static List<FeedItem> generateDUmmyFeeds() {
		List<FeedItem> feedItems = new ArrayList<>();
		feedItems.add(new FeedItem("Wan Clem", R.drawable.driving_a_car, "Posted", "2hr", R.drawable.mercedes_benz, "A very nice mercedez Benz", 15, 35, 15, 22, true));
		feedItems.add(new FeedItem("Sean Parker", R.drawable.man_in_suit, "Shared", "3hr", R.drawable.suits, "Men with class", 19, 30, 10, 28, false));
		feedItems.add(new FeedItem("Ivanka TimberLake", R.drawable.girl_jogging, "Shared", "4hr", R.drawable.joggers, "Awesome joggers", 74, 42, 90, 11, true));
		feedItems.add(new FeedItem("Angelina Blanca", R.drawable.descent, "Posted", "5hr", R.drawable.shoes, "Nice pair of shoes", 18, 39, 20, 25, false));
		feedItems.add(new FeedItem("Bradly Gates", R.drawable.riding_bycle, "Posted", "6hr", R.drawable.power_bike, "A very nice power bike", 15, 35, 15, 22, true));
		return feedItems;
	}

	public static List<ProfileModel> generateUsers()
	{
		String gender1 = "male";
		String gender2 = "female";
		List<ProfileModel> users = new ArrayList<>();
		ArrayList<String> photoURLs = new ArrayList<>();
		photoURLs.add("https://pp.userapi.com/c638424/v638424593/15ad9/SiQb3lYQQrQ.jpg");
		users.add(new ProfileModel("Daria Roman", "Kyiv",
				gender1, gender2,
				"https://pp.userapi.com/c638424/v638424593/15ad9/SiQb3lYQQrQ.jpg",
				photoURLs, generateRandomID()));
		photoURLs.clear();
		photoURLs.add("https://source.unsplash.com/NYyCqdBOKwc/600x800");
		users.add(new ProfileModel("Fushimi Inari Shrine", "Kyoto",
				gender1, gender2,
				"https://source.unsplash.com/NYyCqdBOKwc/600x800",
				photoURLs, generateRandomID()));
		photoURLs.clear();
		photoURLs.add("https://source.unsplash.com/buF62ewDLcQ/600x800");
		users.add(new ProfileModel("Bamboo Forest", "Kyoto",
				gender1, gender2,
				"https://source.unsplash.com/buF62ewDLcQ/600x800",
				photoURLs, generateRandomID()));
		photoURLs.clear();
		photoURLs.add("https://source.unsplash.com/THozNzxEP3g/600x800");
		users.add(new ProfileModel("Brooklyn Bridge", "New York",
				gender1, gender2,
				"https://source.unsplash.com/THozNzxEP3g/600x800",
				photoURLs, generateRandomID()));
		photoURLs.clear();
		photoURLs.add("https://source.unsplash.com/USrZRcRS2Lw/600x800");
		users.add(new ProfileModel("Empire State Building", "New York",
				gender2, gender1,
				"https://source.unsplash.com/USrZRcRS2Lw/600x800",
				photoURLs, generateRandomID()));
		photoURLs.clear();
		photoURLs.add("https://source.unsplash.com/PeFk7fzxTdk/600x800");
		users.add(new ProfileModel("The statue of Liberty", "New York",
				gender2, gender1,
				"https://source.unsplash.com/PeFk7fzxTdk/600x800",
				photoURLs, generateRandomID()));
		photoURLs.clear();
		photoURLs.add("https://source.unsplash.com/LrMWHKqilUw/600x800");
		users.add(new ProfileModel("Louvre Museum", "Paris",
				gender2, gender1,
				"https://source.unsplash.com/LrMWHKqilUw/600x800",
				photoURLs, generateRandomID()));
		photoURLs.clear();
		photoURLs.add("https://source.unsplash.com/HN-5Z6AmxrM/600x800");
		users.add(new ProfileModel("Eiffel Tower", "Paris",
				gender2, gender1,
				"https://source.unsplash.com/HN-5Z6AmxrM/600x800",
				photoURLs, generateRandomID()));
		photoURLs.clear();
		photoURLs.add("https://source.unsplash.com/CdVAUADdqEc/600x800");
		users.add(new ProfileModel("Big Ben", "London",
				gender2, gender1,
				"https://source.unsplash.com/CdVAUADdqEc/600x800",
				photoURLs, generateRandomID()));
		photoURLs.clear();
		photoURLs.add("https://source.unsplash.com/AWh9C-QjhE4/600x800");
		users.add(new ProfileModel("Great Wall of China", "China",
				gender2, gender2,
				"https://source.unsplash.com/AWh9C-QjhE4/600x800",
				photoURLs, generateRandomID()));
		return users;
	}

	public List<ProfileModel> getUsersCards () {
		return usersCards;
	}

	public void setUsersCards (List<ProfileModel> usersCards) {
		this.usersCards = new ArrayList<>();
		this.usersCards.addAll(usersCards);
	}

    /*
    generate random uid
     */

	private static String generateRandomID() {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(targetStringLength);
		for (int i = 0; i < targetStringLength; i++) {
			int randomLimitedInt = leftLimit + (int)
					(random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		return buffer.toString();
	}
}
