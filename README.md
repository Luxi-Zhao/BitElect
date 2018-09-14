# BitElect
BitElect attempts to unite the convenience of mobile voting with the security of the blockchain. Using the mobile application, users can vote for their preferred candidate after going through an identity check, with their votes ultimately being stored in a blockchain on a DE1-SoC board. This repo only contains source code for the Android application.

## Screenshots
<p align="center">
  <img src="https://github.com/Luxi-Zhao/BitElect/blob/master/screenshots/vote_page_ui.png" width="200" />
  <img src="https://github.com/Luxi-Zhao/BitElect/blob/master/screenshots/passport_stepper_ui.png" width="200" />
  <img src="https://github.com/Luxi-Zhao/BitElect/blob/master/screenshots/poll_result_ui.png" width="200" />
  <img src="https://github.com/Luxi-Zhao/BitElect/blob/master/screenshots/blockchain_ui.png" width="200" />
</p>

## Acknowledgements
* Low level passport reading 
  * JMRTD library (https://jmrtd.org)
  * SCUBA library (http://scuba.sourceforge.net/)
* JPEG2000 image decoding 
  * JJ2000 library (https://github.com/stain/jai-imageio-core)
  * Sample source code that shows how to use the library  (https://github.com/tananaev/passport-reader)
* Face detection, QR code detection - Google Mobile Vision API (https://developers.google.com/vision/)
