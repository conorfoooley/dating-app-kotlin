# roove  [![GitHub license](https://img.shields.io/github/license/muramrr/roove)](https://github.com/muramrr/roove/blob/master/LICENSE) [![](https://img.shields.io/badge/minSDK-21-blue)](https://shields.io/) [![](https://img.shields.io/badge/TargetSDK-29-green)](https://shields.io/)

A simple dating app based on tinder-style cards. WIP 91% done.


Used libraries/patterns:
* MVVM pattern;
* Dagger 2;
* RxJava 2;
* Glide;
* Firebase Auth;
* Firestore to store *user, messages* data;
* Firestorage to store *photos*;
* Data pagination;
* Retrofit to access public [Kudago](https://kudago.com) Api; 
* Facebook SDK to login.


![Logo](https://github.com/muramrr/roove/blob/master/media/roove_logo_256.png)

### Setup

Create your own firebase project ([Firebase Guide](https://firebase.google.com/docs/android/setup))

Setup **FirebaseAuth**, **FirebaseFirestore**, **FirebaseStorage** for your project by following guides at link above.

**If you want to use this project for own purposes you should create 3 additional files:**

Path: *app/src/main/res/values*:
* **misc.xml** (your facebook protocol strings, follow guide at [Facebook Developers](https://developers.facebook.com/docs/facebook-login/android/) to enable facebook login)
* **font_certs.xml** (generated by google allowing to use downloadable fonts in application, see more at [Android Developers](https://developer.android.com/guide/topics/ui/look-and-feel/downloadable-fonts))

Path: *%project_root_folder%*
* **key.properties** containing sensative strings related to your firestorage url and encrypt keys (you can use generator to obtain random enc keys)

 *FIREBASE_STORAGE_URL* = "gs:/your url" (your firestorage reference link can be find at *console.firebase/.../project/...yourapp.../storage*)
 
 *KEY_ENCRYPTION_KEY* = "32 lenght enc key"
 
 *VALUE_ENCRYPTION_KEY* = "16 lenght enc key"
 
 *VALUE_ENCRYPTION_VECTOR_KEY* = "16 lenght dec key"


### Explanations

**So many developers, so many minds.**

Also, keep in mind, that *business* module should not contain android-based plugins. It is a pure kotlin module.

*Data* module is an android library.

*ViewModel* shoudn't contain any android imports, except androidx.lifecycle. 

## License

[GitHub license](https://github.com/muramrr/roove/blob/master/LICENSE)


```
Copyright (c) 2020 Andrii Kovalchuk
```
