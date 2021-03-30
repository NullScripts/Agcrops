<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/othneildrew/Best-README-Template">
    <img src="https://github.com/NullScript/Agcrops/blob/main/app/src/main/res/drawable/agcrops_icon.jpg" alt="Logo" width="400" height="400">
  </a>

  <h1 align="center">AGCROPS</h1>

  <p align="center">
    An android application targeted for agricultural sector
    <br />
    <br />
    <a href="https://youtu.be/nbJEsyprcBI">View Demo</a>
    •
    <a href="https://youtu.be/5y3XP2bumKw">Interactive Demo</a>
  •
    <a href="https://github.com/NullScript/Agcrops/issues">Report Bug</a>
    •
    <a href="https://github.com/NullScript/Agcrops/issues">Request Feature</a>
  </p>
</p>

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#introduction">Introduction</a></li>
    <li><a href="#why-agcrops">Why Agcrops</a></li>
    <li><a href="#google-technologies-used">Google Technologies used</a></li>
    <li><a href="#setup">Setup</a></li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#implemented-features">Implemented Features</a></li>
    <li><a href="#future-scope">Future Scope</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact-us">Contact Us</a></li>
  </ol>
</details>

<!-- INTRODUCTION -->
## Introduction

Agcrops is an Android application targeted for agricultural sector which includes Farmers, Daily wage workers, fertilizer shops and the heavy vehicle drivers/providers (such as Tractors). 

<!-- WHY AGCROPS -->
## Why Agcrops

Using Agcrops we intend to provide a platform to farmers using which they can directly contact the buyers and sell their yields at optimum prices. As during Covid19 lockdown most of these trading was completely stopped. It gave us the idea to create a platform through which the farmer can sell its yields during such conditions also. Can interact with the buyers with help of Agcrops and trade is continued. While thinking of this idea we came up with some more such ideas that can be integrated. That are for daily wage workers and tractors. In India, most of the farmers have income below poverty line and have land less than two acres. For them, buying tractor is not economically good. And hence, few of them have the tractors and rest of them rent them for their work. But most of the time the tractor is not available at right time or even if its available, farmers are unaware of it. This causes confusion. Whereas, similar case is seen with Daily wage workers. Sometimes there is ample of work but farmers cannot get workers for it and workers cannot get work in farms just due to lack of interaction. With the help of Agcrops we intend to solve these problems along with facility to buy and sell fertilizers.
Another feature of this app is,buy and sell of fertilizers. Here a licenced fertilizer seller can sell the fertilizers online through this app, the seller puts the selling request and the buyer receives the request based on location and can buy the fertilizers from the nearest available seller.

<!-- GOOGLE TECHNOLOGIES USED -->
## Google Technologies used

* [Android-Studio](https://developer.android.com/studio/)
* [Firebase](https://firebase.google.com/)
* [Firebase Cloud Messanging](https://firebase.google.com/docs/cloud-messaging)
* [Google Cloud Platform API's](https://console.cloud.google.com/apis/)

<!-- SETUP -->
## Setup

Run the command in your terminal
```
git clone https://github.com/NullScript/Agcrops.git
```
Or you can just clone it through [Android Studio](https://developer.android.com/studio) which will be much easier.

<!-- USAGE -->
## Usage

Agcrops provides an online platform for farmers to buy and sell their farm yields even in uncertain times like covid19 lockdowns. It also provides facility to contact and hire the tractors as and when required. Daily wage workers can put their status as free or busy and based on that the farmers can hire them when required. The fertilizer shop owners can also buy and sell the fertilizers to the farmers through Agcrops.

<!-- IMPLEMENTED FEATURES -->
## Implemented Features

Currently we have implemented the buying and selling of the crops. For better understanding please refer to the image given below.

<p align="center">
  <img src="https://github.com/NullScript/Agcrops/blob/images/Crop.png" alt="Logo" width="480" height="270">
</p>

A buyer sends a buying request and then the seller, i.e. the farmer can sell the crop to the corresponding buyer. Additionally, the details are available for the buyer and seller to contact each other.
</br>

The next important thing inside the agriculture sector is the Tractor Renting thing. So, we have built a real time tractor renting application with real time location tracking of the tractor and estimated time of delievery using some Google Cloud Platform API's and Firebase Cloud Messanging

<p align="center">
  <img src="https://github.com/NullScript/Agcrops/blob/images/Tractor.png" alt="Logo" width="480" height="270">
</p>

Inside the image, the one who will rent is online for renting his tractor and the one who wants to rent is going through the confirm cash payment option. Then a FCM request will be send and therefore the renter can decide to rent or not to rent.

<!-- FUTURE SCOPE -->
## Future Scope
  <ol>
    <li>Daily Worker Job Feature</li>
    <li>Payment Gateway</li>
    <li>Support System</li>
    <li>End to End chat system</li>
  </ol>

<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/VeryCoolFeature`)
3. Commit your Changes (`git commit -m 'Add a VeryCoolFeature'`)
4. Push to the Branch (`git push origin feature/VeryCoolFeature`)
5. Open a Pull Request

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.

<!-- CONTACT -->
## Contact Us

NullScripts - [nullscript2021@gmail.com](mailto:nullscripts2021@gmail.com)
