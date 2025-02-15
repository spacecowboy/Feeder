# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.9.1] - 2025-02-15

### 🐛 Bug Fixes & Minor Changes
- Cache images separately to avoid evicting feed responses by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bac8c70f724efc38979b70e0070d0ba2738fe1cf)
- Fixed so pull to refresh does a conditional get if possible by @spacecowboy in [#593](https://github.com/spacecowboy/feeder/pull/593) 
- Improved charset detection logic for full article parsing (#594) by @spacecowboy in [#594](https://github.com/spacecowboy/feeder/pull/594) 
- Fixed broken swipe to mark as read (#600) by @spacecowboy in [#600](https://github.com/spacecowboy/feeder/pull/600) 

### 🌐 Translations
- Translated using Weblate (French) by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/64a7b11a89f4e3564590a7e88e0b32c50afea800)
- Translated using Weblate (Chinese (Simplified Han script)) in [commit](https://github.com/spacecowboy/feeder/commit/a62ce294488959d99b2419b3b5e8721e0ae0cefc)
- Translated using Weblate (Polish) by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/5750fe16741812484fea2b985b716e7b713110ee)
- Translated using Weblate (Hungarian) by @summoner001 in [commit](https://github.com/spacecowboy/feeder/commit/052670eeec24ae7be27230048ee77cbd1c4bd670)
- Translated using Weblate (Bulgarian) by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/38971c82e398b0f587c9529c0a2a97ccad61fcd1)
- Translated using Weblate (German) by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/f326d5af417b8ac23c573adfff871485b9d20360)
- Translated using Weblate (Spanish) by @jesusFx in [commit](https://github.com/spacecowboy/feeder/commit/ee864d098adb35e19579c139b0eb2eea85decdd3)
- Translated using Weblate (Ukrainian) by @andmizyk in [commit](https://github.com/spacecowboy/feeder/commit/2a4f13bd6534c3eaf2f9e8ad5cfc880360ae5a1c)
- Updated Arabic translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8c0858ab17d1c3607d52ae9ed7e330c75d783608)
- Translated using Weblate (Serbian) by @eevan78 in [#591](https://github.com/spacecowboy/feeder/pull/591) 


## [2.9.0] - 2025-02-07

### 🚀 Features
- Added support for anchor links (#567) by @spacecowboy in [#567](https://github.com/spacecowboy/feeder/pull/567) 

### 🐛 Bug Fixes & Minor Changes
- Fixed emulator test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/30b678e833b54c887eb3c0256f26cd739b08375c)
- Removed email catcher on crashes due to abuse by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2fbd336a9ec2a350a9ff749efed0d2210e90886c)
- Moved version catalog to TOML by @spacecowboy in [#500](https://github.com/spacecowboy/feeder/pull/500) 
- Added string resources for on and off by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0e0b890e33c1fc5393ea0b863cf44f2f0016f1a5)
- Removed use of jsoup stringutil by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/16f56e577d4938f1fb008d0108c0d1f13c89134a)
- Fixed feed title not showing on first open (#564) by @spacecowboy in [#564](https://github.com/spacecowboy/feeder/pull/564) 
- Fixed so clicking outside selected text will dismiss it (#565) by @spacecowboy in [#565](https://github.com/spacecowboy/feeder/pull/565) 
- Added support for inline base64 encoded data images by @spacecowboy in [#584](https://github.com/spacecowboy/feeder/pull/584) 
- Implemented rate limiting for all requests (#585) by @spacecowboy in [#585](https://github.com/spacecowboy/feeder/pull/585) 
- Fixed edge case where articles in the feed could be deleted (#576) by @spacecowboy in [#576](https://github.com/spacecowboy/feeder/pull/576) 

### 🌐 Translations
- Updated Hungarian translation using Weblate by @summoner001 in [commit](https://github.com/spacecowboy/feeder/commit/ee0f8a19362a4bbca1c45403c7d867dcd90079e4)
- Updated Esperanto translation using Weblate by @Wiccio in [commit](https://github.com/spacecowboy/feeder/commit/ce92ef9f52961bda4b7aea3c1555a8d168a3d5cd)
- Translated using Weblate (Indonesian) by @rezaalmanda in [#497](https://github.com/spacecowboy/feeder/pull/497) 
- Updated Portuguese (Brazil) translation using Weblate by @KCosta5 in [commit](https://github.com/spacecowboy/feeder/commit/df2045cb1c65b36283eb8e326a60d06377412bfd)
- Updated Greek translation using Weblate by @trlef19 in [#524](https://github.com/spacecowboy/feeder/pull/524) 
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/749fd4fd1d7d7ae3437b4a1e540643145dfb3056)
- Updated Hungarian translation using Weblate by @summoner001 in [commit](https://github.com/spacecowboy/feeder/commit/eb23c5069c366dd03a036750a00d70d92d02b750)
- Updated Chinese (Simplified Han script) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4b12894481a461c9e7b177618b9f6ada7d5a4be5)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/706928e85b5564f7d8ae48c5e8b44a0aefde732c)
- Updated Bulgarian translation using Weblate by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/d595ec8b0fa7514a6e9bb10fa3f45bed3b8c292d)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/b2e2a70d4cf99a460a31b55b67380506add0f60a)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/4ae855460865428f83faf4fce734bb404a191ac6)
- Updated Serbian translation using Weblate by @eevan78 in [commit](https://github.com/spacecowboy/feeder/commit/417bc9eabad70da3309efd6afac0166c0635f259)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/e11610d7fdad3b9c584d79508076c9276a86b7ac)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a39c64fd737ececeb7feda0eab5921409ada4013)
- Updated Ukrainian translation using Weblate by @balaraz in [#536](https://github.com/spacecowboy/feeder/pull/536) 
- Updated translations from Hosted Weblate (#588) by @weblate in [#588](https://github.com/spacecowboy/feeder/pull/588) 

### ❤️  New Contributors
* @renovate[bot] made their first contribution in [#580](https://github.com/spacecowboy/feeder/pull/580)
* @balaraz made their first contribution in [#536](https://github.com/spacecowboy/feeder/pull/536)
* @KCosta5 made their first contribution

## [2.8.1] - 2025-01-18

### 🐛 Bug Fixes & Minor Changes
- Nostr Mention in Readme by @nostrdev-com in [commit](https://github.com/spacecowboy/feeder/commit/bb16f879b399a7da01c568f51d5e17f038b01f75)
- Removing example by @nostrdev-com in [#483](https://github.com/spacecowboy/feeder/pull/483) 
- Changed memory caching of images to be max 50MB instead of 25% of RAM by @spacecowboy in [#480](https://github.com/spacecowboy/feeder/pull/480) 

### 🌐 Translations
- Updated Chinese (Simplified Han script) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4787d55b8df166d88e84488fc4e60305942ae162)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/e2fd08708c063838c2ae48d53a64b60323b1d6f0)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/a022364042cf0137d5b8dbdd03921fd68e1b295a)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/82a2b196e802760c0c308f098ee7bf7ce525061f)
- Updated Hungarian translation using Weblate by @summoner001 in [commit](https://github.com/spacecowboy/feeder/commit/005749529a2b04ae87821b19d1c4df25f422026c)
- Updated Serbian translation using Weblate by @eevan78 in [commit](https://github.com/spacecowboy/feeder/commit/734535c24b7606888963abcc41270c687c705cfd)
- Updated Czech translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/00e2909cc360d66179f46440daa6563a7f02cbb8)
- Updated Spanish translation using Weblate by @jesusFx in [commit](https://github.com/spacecowboy/feeder/commit/30330d78232269769400994b5c151e5bc11b7ffe)
- Updated Indonesian translation using Weblate by @zmni in [#482](https://github.com/spacecowboy/feeder/pull/482) 

### ❤️  New Contributors
* @jesusFx made their first contribution
* @yukibtc made their first contribution in [#489](https://github.com/spacecowboy/feeder/pull/489)
* @nostrdev-com made their first contribution in [#483](https://github.com/spacecowboy/feeder/pull/483)

## [2.8.0] - 2025-01-04

### 🐛 Bug Fixes & Minor Changes
- Added Nostr feed support (#471) by @KotlinGeekDev in [#471](https://github.com/spacecowboy/feeder/pull/471) 
- Fixed emulator test crash by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fef585f8a9312339416b600555594edd9d901d2b)

### 🌐 Translations
- Updated Esperanto translation using Weblate by @Wiccio in [commit](https://github.com/spacecowboy/feeder/commit/c6d9c85078d02b3286c8a64669ac94f044c9c6b1)
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/e00933890dd056b1906fdebb10f8d7d52261510e)
- Translated using Weblate (Tamil) by @TamilNeram in [#470](https://github.com/spacecowboy/feeder/pull/470) 
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/6dbedc6ce465be3334f6b016b99d245caaa8a191)
- Updated Hungarian translation using Weblate by @summoner001 in [commit](https://github.com/spacecowboy/feeder/commit/e6ab1a1bfb0ac560de79b62c2f403943015e2f48)
- Updated Bulgarian translation using Weblate by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/27585e4825ccf4f8a79efa37b6133722e4b0c413)
- Updated Swedish translation using Weblate by @spacecowboy in [#479](https://github.com/spacecowboy/feeder/pull/479) 

### ❤️  New Contributors
* @KotlinGeekDev made their first contribution in [#471](https://github.com/spacecowboy/feeder/pull/471)
* @TamilNeram made their first contribution in [#470](https://github.com/spacecowboy/feeder/pull/470)

## [2.7.4] - 2024-12-20

### 🐛 Bug Fixes & Minor Changes
- Fixed tablet being hardcoded to card article style by @spacecowboy in [#466](https://github.com/spacecowboy/feeder/pull/466) 
- Switched to JobScheduler to fix crash on older versions of Android (#465) by @spacecowboy in [#465](https://github.com/spacecowboy/feeder/pull/465) 


## [2.7.3] - 2024-12-18

### 🐛 Bug Fixes & Minor Changes
- Removed foreground permission since it wasn't used by @spacecowboy in [#459](https://github.com/spacecowboy/feeder/pull/459) 
- Fixed missing spaces inside some tags by @spacecowboy in [#460](https://github.com/spacecowboy/feeder/pull/460) 
- Fixed crash when a table was empty by @spacecowboy in [#461](https://github.com/spacecowboy/feeder/pull/461) 


## [2.7.2] - 2024-12-16

### 🐛 Bug Fixes & Minor Changes
- Added setting for opening feeds drawer when pressing FAB by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cd8f70b4ea3422fcd11d0fbf23985c207c95d27c)
- Added feed item styles for tablets (no longer hidden on tablet) by @spacecowboy in [#440](https://github.com/spacecowboy/feeder/pull/440) 
- [Hotfix] Show snippet of feed instead of "Found nothing to fetch" by @asl97 in [commit](https://github.com/spacecowboy/feeder/commit/3400902936b7bdfae3884265fbea71c14bf8c934)
- Add an user noticable notice for why the snippet is shown by @asl97 in [commit](https://github.com/spacecowboy/feeder/commit/814e51f8f286aef1bc0559d233d565bcf3c3ef9c)
- Run ./gradlew ktlintformat by @asl97 in [#447](https://github.com/spacecowboy/feeder/pull/447) 

### 🌐 Translations
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/ba2a09f724ca2e3544620b3c32bf302b8ac1a442)
- Updated Greek translation using Weblate by @trlef19 in [commit](https://github.com/spacecowboy/feeder/commit/f7eccb3ff922af35257263f45d8b929281f08898)
- Updated Spanish translation using Weblate by @Simx72 in [commit](https://github.com/spacecowboy/feeder/commit/781838d71134e74917b3b14ae5cc338911f8d43e)
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/2d864ec38e7b1789bab35b260149565790beb590)
- Updated Esperanto translation using Weblate by @Wiccio in [commit](https://github.com/spacecowboy/feeder/commit/00efdb1d9d91e528ce90dc22ced571eb39edfc22)
- Updated Finnish translation using Weblate by @Ricky-Tigg in [commit](https://github.com/spacecowboy/feeder/commit/8749fc3819e908fcf780b4fadc73fbc7f0f0186f)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/9bded05ea25092bd2114986dce8b49eb60d49a58)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/fc652d0a51e826391cdc178dc2adad417583f8ce)
- Updated Chinese (Simplified Han script) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b7e60ecfe9ecc14259fe9e20ea379beecd58e86a)
- Updated Serbian translation using Weblate by @eevan78 in [commit](https://github.com/spacecowboy/feeder/commit/b80d561a98f626e9b1b103986dab1998d7b86619)
- Updated Hungarian translation using Weblate by @summoner001 in [commit](https://github.com/spacecowboy/feeder/commit/6adb9aa5060f021ea91ac9b923e9e9b9568263f1)
- Updated Bulgarian translation using Weblate by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/8812cb3b6e9024b817de4695b693610f30fc631a)
- Updated Chinese (Traditional Han script) translation using Weblate by @serAKL16lysA in [commit](https://github.com/spacecowboy/feeder/commit/066a2783d487d3a6e76a22aa9f20cb31c94be46c)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/f661a1ef55bb4aae7dee577b2f862d111f373353)
- Updated Finnish translation using Weblate in [#437](https://github.com/spacecowboy/feeder/pull/437) 

### ❤️  New Contributors
* @asl97 made their first contribution in [#447](https://github.com/spacecowboy/feeder/pull/447)
* @serAKL16lysA made their first contribution
* @Ricky-Tigg made their first contribution

## [2.7.1] - 2024-11-29

### 🐛 Bug Fixes & Minor Changes
- Added Perplexity AI support (need to set custom URL) (#433) by @anod in [#433](https://github.com/spacecowboy/feeder/pull/433) 

### 🌐 Translations
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/99ca73cb17f16d8f989282e47a03004552678319)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/a53bdd2a215c98cc041710e75a95178fd27fd0b1)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/93f7ff7b52c8d821ca5f6f0b614f164a391cb20c)
- Updated Chinese (Simplified Han script) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9ebf88737cbdc959ff7229304c1febe829535c92)
- Updated Greek translation using Weblate by @trlef19 in [commit](https://github.com/spacecowboy/feeder/commit/c1d91bc9dff43bba66be5e82eab17ed47891d7a7)
- Updated Hungarian translation using Weblate by @summoner001 in [commit](https://github.com/spacecowboy/feeder/commit/3381d81a5ba53234ba1a793927aa104c5e2e44c8)
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e544e4f4f2689089d69e1e1a32183edfa2a8f2e2)
- Updated Bulgarian translation using Weblate by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/b48d556f5b4125105e29e3ad3fc82ed2450b3cb4)
- Updated Czech translation using Weblate by @pchelium in [#423](https://github.com/spacecowboy/feeder/pull/423) 
- Updated Serbian translation using Weblate by @eevan78 in [commit](https://github.com/spacecowboy/feeder/commit/6b19ca53e8a54e8a9dae1baaad537de5a7674a96)
- Updated Bulgarian translation using Weblate by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/666a026707cd0e04c2db88f1bcb1572a549d93b3)
- Translated using Weblate (Danish) in [commit](https://github.com/spacecowboy/feeder/commit/338ec167a6aabdcd7a45b26563a3f96f9546973f)
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/e085841c51e08e51f94ccce70e385e6a91f13e44)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/27c73f1b5f8860aa0e8754caa483a7f30e790f63)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/55f6499ba8ab80755e4ca929bf62dc4604014c9a)
- Updated Chinese (Simplified Han script) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/820af2d3b1b9ab4a8c3b48195765df2ce4d373e2)
- Updated Hungarian translation using Weblate by @summoner001 in [#431](https://github.com/spacecowboy/feeder/pull/431) 


## [2.7.0] - 2024-11-17

### 🐛 Bug Fixes & Minor Changes
- Feed title empty on initial app start by @svenjacobs in [#388](https://github.com/spacecowboy/feeder/pull/388) 
- Open drawer after hitting fab mark all articles fab (#386) by @maksimowiczm in [#386](https://github.com/spacecowboy/feeder/pull/386) 
- Cleaned up some sync code by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a0d0583470bdcdf664e8c41110824cc1bcdc6123)
- Added article summary with OpenAI integration (#399) by @anod in [#399](https://github.com/spacecowboy/feeder/pull/399) 
- Changed default for read-filter to reduce confusion for new users by @spacecowboy in [#387](https://github.com/spacecowboy/feeder/pull/387) 

### 🌐 Translations
- Updated Norwegian Bokmål translation using Weblate by @weblate in [commit](https://github.com/spacecowboy/feeder/commit/7f17b353e5a185b8a8bf3544b6f72a3e9136a16d)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d16c3677e2413c8a3675678c0f9cb2a6ea6e21b7)
- Updated Hungarian translation using Weblate by @summoner001 in [commit](https://github.com/spacecowboy/feeder/commit/6f5fdd3ba01ff9327ed7a9bec3fda918369a796f)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e9eb6968459b41e83d0bf41f953990580887e877)
- Updated Swedish translation using Weblate by @bittin in [#378](https://github.com/spacecowboy/feeder/pull/378) 
- Updated Bulgarian translation using Weblate by @trunars in [#389](https://github.com/spacecowboy/feeder/pull/389) 
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/0c97a6e8260aeea56cb32a9ea34d06da223e60d7)
- Updated Esperanto translation using Weblate by @Wiccio in [commit](https://github.com/spacecowboy/feeder/commit/574c478d6fb9343e2adefa65b56e86ff5e576e99)
- Updated Bulgarian translation using Weblate by @trunars in [#397](https://github.com/spacecowboy/feeder/pull/397) 
- Updated Spanish translation using Weblate by @532910 in [#413](https://github.com/spacecowboy/feeder/pull/413) 
- Updated Greek translation using Weblate by @trlef19 in [commit](https://github.com/spacecowboy/feeder/commit/10417ac9750569e891fd2a8ccff95c4516358e04)
- Translated using Weblate (French) by @Matth7878 in [#421](https://github.com/spacecowboy/feeder/pull/421) 

### ❤️  New Contributors
* @532910 made their first contribution in [#415](https://github.com/spacecowboy/feeder/pull/415)
* @maksimowiczm made their first contribution in [#386](https://github.com/spacecowboy/feeder/pull/386)

## [2.6.33] - 2024-10-09

### 🐛 Bug Fixes & Minor Changes
- Added github issue templates by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/63b1b7ab8da1f4da28e612957d207cc0da638137)
- Changed so bug report button opens github issues instead of email by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/350f599b8eb7e764355cc564c590fe43dc49ffb2)

### 🌐 Translations
- Updated Russian translation using Weblate by @aleksey-saenko in [commit](https://github.com/spacecowboy/feeder/commit/0fc8893a9c5161e24dee462bf2be7355dfee5263)
- Updated Hungarian translation using Weblate by @summoner001 in [#366](https://github.com/spacecowboy/feeder/pull/366) 
- Updated Finnish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/ee7652e65cbeba33fc9992ad0c5e3a949f53901c)
- Updated Bulgarian translation using Weblate by @trunars in [#371](https://github.com/spacecowboy/feeder/pull/371) 

### ❤️  New Contributors
* @summoner001 made their first contribution in [#366](https://github.com/spacecowboy/feeder/pull/366)
* @aleksey-saenko made their first contribution

## [2.6.32] - 2024-09-22

### 🐛 Bug Fixes & Minor Changes
- Fixed runtime issue in Kotlin code by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/050f3cff0dd18f76bff7448d61a4abff84e6a157)
- Upgraded Android Gradle Plugin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/77ee1febc64cdf621d61630b530226c29336a285)

### 🌐 Translations
- Translated using Weblate (Dutch) by @fincentxyz in [#365](https://github.com/spacecowboy/feeder/pull/365) 

### ❤️  New Contributors
* @fincentxyz made their first contribution in [#365](https://github.com/spacecowboy/feeder/pull/365)

## [2.6.31] - 2024-08-20

### 🐛 Bug Fixes & Minor Changes
- Added scrollbar to reader by @spacecowboy in [#350](https://github.com/spacecowboy/feeder/pull/350) 

### 🌐 Translations
- Updated Catalan translation using Weblate in [#342](https://github.com/spacecowboy/feeder/pull/342) 
- Updated Russian translation using Weblate by @Drsheppard01 in [#346](https://github.com/spacecowboy/feeder/pull/346) 
- Updated Ukrainian translation using Weblate in [#349](https://github.com/spacecowboy/feeder/pull/349) 

### ❤️  New Contributors
* @Drsheppard01 made their first contribution in [#346](https://github.com/spacecowboy/feeder/pull/346)

## [2.6.30] - 2024-07-31

### 🐛 Bug Fixes & Minor Changes
- Explicitly silenced notifications by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e41f07369ce5506911453876d812a972bc1eaae2)

### 🌐 Translations
- Translated using Weblate (Italian) by @Wiccio in [commit](https://github.com/spacecowboy/feeder/commit/78dac5947c22a36ebb1ee92dc89eabfb7ca636a6)
- Updated Romanian translation using Weblate by @simonaiacob in [#327](https://github.com/spacecowboy/feeder/pull/327) 
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/371ba2fb314d163f3ac781236d1e1d29a3a7d0b1)
- Updated Portuguese (Brazil) translation using Weblate by @edxkl in [#334](https://github.com/spacecowboy/feeder/pull/334) 
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/13b2fd65984f1d2e4dcf3ce6bac8b329c901150a)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e07fe8303b3df4c3a34ff465334c3a90b7268aca)
- Updated Bulgarian translation using Weblate by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/d9f8189804b203663fda4a2ceb98a13b3dbe4be6)
- Updated Spanish translation using Weblate by @gallegonovato in [#336](https://github.com/spacecowboy/feeder/pull/336) 


## [2.6.29] - 2024-07-01

### 🌐 Translations
- Updated Spanish translation using Weblate by @Simx72 in [commit](https://github.com/spacecowboy/feeder/commit/59c675b3094f422ec2541dea5834d9c64f14c9ee)
- Updated Greek translation using Weblate by @trlef19 in [commit](https://github.com/spacecowboy/feeder/commit/6ce06cd04248c6529b0a3673036f762e86b10936)
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/f6cb7f7952abe2e144c820db09cad43e69a68b5b)
- Translated using Weblate (Italian) by @Wiccio in [#313](https://github.com/spacecowboy/feeder/pull/313) 
- Updated Serbian translation using Weblate by @eevan78 in [#326](https://github.com/spacecowboy/feeder/pull/326) 

### ❤️  New Contributors
* @Wiccio made their first contribution in [#313](https://github.com/spacecowboy/feeder/pull/313)
* @Simx72 made their first contribution

## [2.6.28] - 2024-06-16

### 🐛 Bug Fixes & Minor Changes
- Fixed order of modifiers by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0c2dfbccaf8107799a6d0d95f2da205625ab01d3)
- Fixed crash with table spans by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/98f3fa8a4a8b56397cedf6f7470204e3de8641c3)
- Fixed unread count in title for tags (#308) by @svenjacobs in [#308](https://github.com/spacecowboy/feeder/pull/308) 
- Fixed full text parsing sporadically showing error message by @spacecowboy in [#311](https://github.com/spacecowboy/feeder/pull/311) 
- Added support for RTL tables by @spacecowboy in [#312](https://github.com/spacecowboy/feeder/pull/312) 

### 🌐 Translations
- Updated Czech translation using Weblate by @pchelium in [commit](https://github.com/spacecowboy/feeder/commit/939960bbadcdd55efd7bf9bfb478c9b0840391be)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/9fbe413d7a3228da51964e7c13dc9ef54a92299f)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/4c1323277a8443d991e3a8e6b626892d23783178)
- Updated Bulgarian translation using Weblate by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/0194e5452e506d8bf5d879d0a9d206b86484c86f)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e96270d63f66a307571c9799bd231fb3aab575e9)
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/6712f8c4b14e29c7c5c4fad685d4119ea08f9a85)
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/46f301b8137525e55e529e90e2f4d3017331f0d6)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/825b37ea21f5b40faa54d594f386598bdf2f76d9)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e8ddc8ea7fa55e6c9a167b09e17f58ad494aa6e8)
- Updated Hindi translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/57b2eedcae1c7990a13b24f60743ca56673fe725)
- Updated Swedish translation using Weblate by @bittin in [commit](https://github.com/spacecowboy/feeder/commit/ca8d841c89ddb27d447d017bcca7a6eab062b777)
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/257138d4eccda4547b0ead4f9d7c3f43938ced04)
- Updated Arabic translation using Weblate in [#304](https://github.com/spacecowboy/feeder/pull/304) 


## [2.6.27] - 2024-06-11

### 🐛 Bug Fixes & Minor Changes
- Fixed nested content inside blockquotes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f23390ed01e14f9679f5b37bedd6edc38a9d4548)
- Added option to show unread count in title (#300) by @svenjacobs in [#300](https://github.com/spacecowboy/feeder/pull/300) 
- Fixed crash for some images by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5bdd17043902ed071a8659e788fe6fba77443665)
- Added German translation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/35004eee574f45f5b2dc5a6d5db3d9f72c57285b)

### 🌐 Translations
- Updated Vietnamese translation using Weblate by @ngocanhtve in [#299](https://github.com/spacecowboy/feeder/pull/299) 

### ❤️  New Contributors
* @svenjacobs made their first contribution in [#300](https://github.com/spacecowboy/feeder/pull/300)

## [2.6.26] - 2024-06-06

### 🐛 Bug Fixes & Minor Changes
- Fixed email links not opening email client by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/769423012d45eb1179a499358b9419c88977289a)
- Added support for iframes inside figures by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4a96932ecbc6a9224de28fa32d347fb5bf06cbba)
- Fixed parsed width/height of iframes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8ac5fa68fb5ee6c2d701dbcafc7b94a2cde9c25a)
- Fixed some crashes related to article viewing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/57490d9238e36f268a80cc8b184b1becbc3b0548)
- Fixed a crash in reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4bfe0f9f3775c7984922b3752324ee1d789a1015)
- Fixed number of columns for tablets by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0277df6644f221ec9e4cded9714a2d11fc3169f2)
- Fixed too large images on tablets by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4c09edb7cfb22a98420f4a11bee6ae5b29525db4)

### 🌐 Translations
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/fb9541e5a86c3d6324e10d8e90e10642d8a34071)
- Updated Czech translation using Weblate by @pchelium in [commit](https://github.com/spacecowboy/feeder/commit/cf1ae0f52dd9387e00b0f760d79030377a373195)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/7fd77f47535263aaadca14a18570139995dc181b)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/f28fbf1ac01c109f517ac586d339f8186b70b548)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/554eaa5f8569d4c091dd2db73119266f7ef34462)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/85001e889577f5494c1885ae7e3f5a8cb7087235)
- Updated Swedish translation using Weblate by @bittin in [commit](https://github.com/spacecowboy/feeder/commit/c26f657f2f09545d62f86f0fc69bd84df5a81cc7)
- Updated Hindi translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4b89f1d483ab49aec08f7b0645ea628e100205c0)
- Updated Galician translation using Weblate in [#294](https://github.com/spacecowboy/feeder/pull/294) 


## [2.6.25] - 2024-06-03

### 🐛 Bug Fixes & Minor Changes
- Updated release pipeline to always publish APKs even if Play store is dumb by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dea0a709250562cae6949f5d40f46b5b564f49de)
- Fixed release pipeline syntax by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/26b71e8dd7e1c64f7a58d3d63be1aa346479416a)
- Added ktlint to gradle by @spacecowboy in [#288](https://github.com/spacecowboy/feeder/pull/288) 
- Ensured retry-after is respected, even when feeds share the same host by @spacecowboy in [#292](https://github.com/spacecowboy/feeder/pull/292) 
- Rewrote reader layout engine. Adds real table support. by @spacecowboy in [#293](https://github.com/spacecowboy/feeder/pull/293) 
- Fixed test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e76526be95df57bc1a080f0865859b095c97223b)

### 🌐 Translations
- Updated Galician translation using Weblate in [#277](https://github.com/spacecowboy/feeder/pull/277) 
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d7da5ad37b9ddeb1f622ff8d712ba80d681b19d0)
- Updated Greek translation using Weblate by @trlef19 in [commit](https://github.com/spacecowboy/feeder/commit/817acf02fabfb0ba3d000a146d509db3e3347b4b)
- Updated Chinese (Traditional) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/eb7fc21d5aab4f179adfcc07863ecee7b35cd97a)
- Updated Serbian translation using Weblate by @eevan78 in [#286](https://github.com/spacecowboy/feeder/pull/286) 
- Translated using Weblate (Serbian) by @eevan78 in [#289](https://github.com/spacecowboy/feeder/pull/289) 

### ❤️  New Contributors
* @trlef19 made their first contribution

## [2.6.24] - 2024-04-28

### 🐛 Bug Fixes & Minor Changes
- Fixed performance when many entries in blocklist by @spacecowboy in [#272](https://github.com/spacecowboy/feeder/pull/272) 


## [2.6.23] - 2024-04-21

### 🐛 Bug Fixes & Minor Changes
- Upgraded some dependency versions by @spacecowboy in [#262](https://github.com/spacecowboy/feeder/pull/262) 
- Fixed sync indicator: should now stay on screen as long as sync is running by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e69ed1804c37f3ade9dc170f2598708f2178faee)
- Fixed deprecation warnings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/10358f2097033fe3e5f3b73ce082602c8a4de4ce)
- Removed unused proguard rule by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/05e1066cb7e83b87f1099e7ef932b798c38d36e3)
- Fixed broken navigation after version upgrade by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8d87a2a1d8d6b3d239554603aee46cca7b913844)
- Fixed foreground service changes in Android 14 by @spacecowboy in [#267](https://github.com/spacecowboy/feeder/pull/267) 
- Fixed Saved Articles count only showing unread instead of total by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7939495a7decc5f8421875260eab85f7c63e9157)

### 🌐 Translations
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/42e567d5d82b82c909acf7a7b1ee12a35219f851)
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/e699f62ab86b1d4bd265333c91bf34046a12f4f8)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/b56e987b8d51bd4accedfb87a8e4e4de577b6543)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/67ab5429ab25116ec689ace0993ba6ea03350a5f)
- Translated using Weblate (Basque) in [#261](https://github.com/spacecowboy/feeder/pull/261) 
- Translated using Weblate (Vietnamese) by @ngocanhtve in [#268](https://github.com/spacecowboy/feeder/pull/268) 


## [2.6.22] - 2024-04-12

### 🐛 Bug Fixes & Minor Changes
- Fixed so sync will never run when no network available by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/166390e5d16f67bbd3e13eaa5b2710d9bceeed74)
- Fixed performance of NavDrawer. If you have many feeds you will notice by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eac1b124757867a132f2a546614c715130a14061)
- Fixed resource usage during sync. It might be slower now though. by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/178ea561df93c84b352e3a16633ab2e5f16e711f)
- Fixed a resource leak with OkHTTP by @spacecowboy in [#259](https://github.com/spacecowboy/feeder/pull/259) 

### 🌐 Translations
- Translated using Weblate (Portuguese (Brazil)) by @supercaralegal in [commit](https://github.com/spacecowboy/feeder/commit/ea828a076712d64c37d24b43e95f307ba9cbebd3)
- Updated Portuguese (Brazil) translation using Weblate by @lucasmz-dev in [commit](https://github.com/spacecowboy/feeder/commit/b62bf5810f0f1e836a81d4309c862e390a6e58cb)
- Updated Italian translation using Weblate by @atilluF in [#255](https://github.com/spacecowboy/feeder/pull/255) 

### ❤️  New Contributors
* @lucasmz-dev made their first contribution

## [2.6.21] - 2024-04-02

### 🐛 Bug Fixes & Minor Changes
- Fixed Settings preview by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0d564b74b086411b569d5e3777b860cc42bd5cb2)
- Tweaked Cache-Control headers to respect site headers even more by @spacecowboy in [#252](https://github.com/spacecowboy/feeder/pull/252) 

### 🌐 Translations
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0dec77a1e04da78c8f9c1f846384b2799585d90a)
- Updated Bulgarian translation using Weblate by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/b8eb2edd9321294e37982fe71f970dd2275ba602)
- Updated Hungarian translation using Weblate by @zoli111 in [commit](https://github.com/spacecowboy/feeder/commit/2556b909e89cd9de779994bb0c69813b3a6b55f5)
- Updated Ukrainian translation using Weblate by @Serega124 in [commit](https://github.com/spacecowboy/feeder/commit/f8961a42018c4778afc5364b2e4a803c254ea4b1)
- Updated Portuguese (Brazil) translation using Weblate by @supercaralegal in [commit](https://github.com/spacecowboy/feeder/commit/7e7febbf34272224ea95859e0af3392dbbc3b722)
- Updated Lithuanian translation using Weblate by @psukys in [commit](https://github.com/spacecowboy/feeder/commit/0c2c3a8b5b3bc165e3731bca7bfc4f2ec9202de1)
- Updated Greek translation using Weblate by @costa-b in [#245](https://github.com/spacecowboy/feeder/pull/245) 

### ❤️  New Contributors
* @costa-b made their first contribution in [#245](https://github.com/spacecowboy/feeder/pull/245)
* @psukys made their first contribution
* @supercaralegal made their first contribution

## [2.6.20] - 2024-03-17

### 🐛 Bug Fixes & Minor Changes
- Changed "already read" label to "read" instead by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3684b5d7da9f177f8df9cd3c0db0b03196083882)
- Added select all button to delete feed dialog (#234) by @dracarys18 in [#234](https://github.com/spacecowboy/feeder/pull/234) 
- Fixed talkback for delete dialog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/233d6ce988f7c3b35d20bf9c528a463f47ec8a71)
- Fixed some timezone handling in publication dates by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/edc8966daa6128bb81e0403d93a949b82e77b5d3)
- Tried to make a test less flaky by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0845a3b41e66789c2fce0ed27393b8d937621210)
- Added new article style: compact card layout (#243) by @anod in [#243](https://github.com/spacecowboy/feeder/pull/243) 
- Made feed parsing more lenient because I'm sick of bug reports for sites with incorrect content-types by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e0219f24c62d13617634dec832c50983f00ee4a5)

### 🌐 Translations
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8dd7ee20dd12db7358185c837ebf525c12743d8d)
- Updated Hungarian translation using Weblate by @zoli111 in [commit](https://github.com/spacecowboy/feeder/commit/2d57c6355c084531436329f519bc38b6959f73a7)
- Translated using Weblate (Thai) by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/3d094c7d61e2dfca0ea52e07c7ab6ffc163fb410)
- Translated using Weblate (Dutch) by @5mikachu in [commit](https://github.com/spacecowboy/feeder/commit/d3f877f0b8060b7057b896727c1ab1d357e8e7df)
- Updated Czech translation using Weblate by @pchelium in [commit](https://github.com/spacecowboy/feeder/commit/72fdcc0f4e54bb5545b7ebe88e8805081f656bbd)
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/6e7caea3d42fa2c38c11d4375cbbc28d14032d93)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/c4a15c318715f3acfb086385044136be4265a733)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/f13c3c3fc9b37dfb38102d0b1769b3d7d1771d6a)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/95c0b9100ab774e4cdcb637d4bd769103a2e0c27)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/4df745ff11175a9da8ca74d447ba3d1889d5559e)
- Updated Hindi translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/75247321d855f1bfb374867203267d242b2eac9a)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a83ad85f4e8c44b8ff219afad91ba7ad5f49763a)
- Updated Japanese translation using Weblate by @spacecowboy in [#235](https://github.com/spacecowboy/feeder/pull/235) 

### ❤️  New Contributors
* @5mikachu made their first contribution
* @anod made their first contribution in [#243](https://github.com/spacecowboy/feeder/pull/243)
* @dracarys18 made their first contribution in [#234](https://github.com/spacecowboy/feeder/pull/234)

## [2.6.19] - 2024-03-10

### 🐛 Bug Fixes & Minor Changes
- Changed feed parsing library to Gofeed by @spacecowboy in [#211](https://github.com/spacecowboy/feeder/pull/211) 

### 🌐 Translations
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/4f6116ee0107a1416ebe65be5e0b2b2efb853a5b)
- Updated Bulgarian translation using Weblate by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/1823402c5146e38787873a784e90b7a7ad27e76c)
- Translated using Weblate (Portuguese (Brazil)) by @edxkl in [commit](https://github.com/spacecowboy/feeder/commit/b128dfdc00c1206c090ad060e31d091001277a79)
- Translated using Weblate (Czech) in [commit](https://github.com/spacecowboy/feeder/commit/ac47c2b7bf84c8af4cc2524c267dc2c861d63ceb)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/03b6832434e15c8bc1426353da6a859fd182fe04)
- Updated Ukrainian translation using Weblate by @Serega124 in [#229](https://github.com/spacecowboy/feeder/pull/229) 


## [2.6.18] - 2024-03-04

### 🐛 Bug Fixes & Minor Changes
- Added skip duplicates as option for feeds by @spacecowboy in [#225](https://github.com/spacecowboy/feeder/pull/225) 

### 🌐 Translations
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/abf942ea142a47e69df62f646e352e332db7476a)
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/425fa2fe7b197fe35f5fbea7ebce42964315ee51)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/2a4acaef996b0b17c3e32a2e4a6159454f955f3d)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/e9d7eee29dbe0b1460b85e08ad82c5dc46b5e18a)
- Updated Hindi translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/386b3bc5bb5718e1658d3bc4f08e02c6da55362b)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/747dba1dd035fe5a8b0862362f37c6dc35547a2c)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0fa8c78e8b098cb6b24d8c7415d1fb6d4b55dece)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/dc9417bb9f1709ea7983f1105664537042b5ae49)
- Updated Hungarian translation using Weblate by @mdvhimself in [#226](https://github.com/spacecowboy/feeder/pull/226) 
- Updated Czech translation using Weblate by @pchelium in [commit](https://github.com/spacecowboy/feeder/commit/ff73f6d93f149d29dd1264728301c1c4ab3ae59e)
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3ae52e2bea13462c53d89e02dfb1ae1450489165)
- Translated using Weblate (Hungarian) by @mdvhimself in [#227](https://github.com/spacecowboy/feeder/pull/227) 


## [2.6.17] - 2024-02-25

### 🐛 Bug Fixes & Minor Changes
- Updated README and store descriptions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bcac30c7e16ccead9ce94aba0f2aa27523060085)
- Changed so duplicate stories are ignored by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c6b79e3deccc4e8074cc95199ed29627c5216fd3)
- Fixed articles getting mixed with other articles sometimes by @spacecowboy in [#218](https://github.com/spacecowboy/feeder/pull/218) 

### 🌐 Translations
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/5a3b22222d7755ab73f3b31b24f7aa5e0e80d483)
- Translated using Weblate (Hindi) in [commit](https://github.com/spacecowboy/feeder/commit/aa584e20955f83ef7e24f26295179938763c9c5f)
- Translated using Weblate (Basque) by @weblate in [commit](https://github.com/spacecowboy/feeder/commit/b65a7a11e82dcaa6dce69ef3218b85bcb292bb4f)
- Updated Finnish translation using Weblate by @jere-a in [commit](https://github.com/spacecowboy/feeder/commit/7b6bc6b292d322b833f9475079f6fd90ee22cbb7)
- Translated using Weblate (German) in [commit](https://github.com/spacecowboy/feeder/commit/22759475309f724e2bb4979be8455ec856fae27f)
- Translated using Weblate (Spanish) by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/4b412585885e4f95f48a58f7f0bc5e3c319fb34d)
- Translated using Weblate (Polish) by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/735920e6b726e8264b003f729301f4dcf68dc62d)
- Translated using Weblate (Turkish) by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/85ace16d109a9bc46ef4c5fcfec91b847d620380)
- Translated using Weblate (Ukrainian) by @Serega124 in [commit](https://github.com/spacecowboy/feeder/commit/d73924408560a68863b4ce645006b3bd613708d1)
- Translated using Weblate (Bulgarian) by @trunars in [commit](https://github.com/spacecowboy/feeder/commit/a52a8f0440fec8020f0846506ce45adfc06fce3a)
- Updated Russian translation using Weblate by @homocomputeris in [commit](https://github.com/spacecowboy/feeder/commit/cac3ead4ad6b2f8be28b086268dfedb68c3df706)
- Updated Swedish translation using Weblate by @bittin in [#214](https://github.com/spacecowboy/feeder/pull/214) 
- Updated Hungarian translation using Weblate by @zoli111 in [#219](https://github.com/spacecowboy/feeder/pull/219) 
- Translated using Weblate (Russian) in [#224](https://github.com/spacecowboy/feeder/pull/224) 

### ❤️  New Contributors
* @homocomputeris made their first contribution
* @jere-a made their first contribution

## [2.6.16] - 2024-02-10

### 🐛 Bug Fixes & Minor Changes
- Added ability to export saved articles by @spacecowboy in [#205](https://github.com/spacecowboy/feeder/pull/205) 
- Update README screenshot references by @amjerm in [#208](https://github.com/spacecowboy/feeder/pull/208) 

### 🌐 Translations
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/54334b8267a478ea4623c3e359728e2bbb94c980)
- Updated Czech translation using Weblate by @pchelium in [commit](https://github.com/spacecowboy/feeder/commit/e971b3409cc72c198ae344402e37a1fd088c0679)
- Updated Portuguese (Brazil) translation using Weblate by @edxkl in [commit](https://github.com/spacecowboy/feeder/commit/9067ff5ddd31f7762d9a36a44233931babcde0f9)
- Updated Hungarian translation using Weblate by @zoli111 in [commit](https://github.com/spacecowboy/feeder/commit/02b011622996f7185e53b355867a58881484ac72)
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/304771add4ce2821ca3eb5a075650a6edd602d25)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/dd821aad5fec43de5d63b09018af2d2bf33827ec)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/832cfce5019a3b5fefd0f7523088ebe55748440f)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/4976c3ee7e5661506ec700e8ae0ac24032ae1b56)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/01ccc9d25240acccf9ffa52e52cca81459e2cd2b)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3baf10306ee062103a94fde4f32bd5845a542a00)
- Updated Italian translation using Weblate by @mattiamari in [commit](https://github.com/spacecowboy/feeder/commit/e32b060dc6da80022cc9835036e83053c4edb631)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/1e571b0a5fd3a60caef447124a1206dc26fed272)
- Updated Ukrainian translation using Weblate by @Serega124 in [#198](https://github.com/spacecowboy/feeder/pull/198) 

### ❤️  New Contributors
* @mattiamari made their first contribution
* @amjerm made their first contribution in [#208](https://github.com/spacecowboy/feeder/pull/208)

## [2.6.15] - 2024-01-23

### 🐛 Bug Fixes & Minor Changes
- Added Galician language component by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a2465ad46fb5834fbfa51962dfe06dd770cc8a04)
- Removed decorative icons from TalkBack by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d31d028dac0c9b20db3cbf6195451f9ab1996cff)
- Added invisible close menu buttons for TalkBack to all dropdown menus by @spacecowboy in [#190](https://github.com/spacecowboy/feeder/pull/190) 
- Added LeakCanary to debug builds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d5a7234e936e616262cbef1d612230ac6006119e)
- Testing not deleting galician language by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3f9d0507f3854088b04c6a8ddb2307fa6534a718)

### 🌐 Translations
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/39187233e65861e418700d12bc83cd451b3079bb)
- Updated Hungarian translation using Weblate by @zoli111 in [#184](https://github.com/spacecowboy/feeder/pull/184) 
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b0aa7a9c72acbacc98a4423ba4c6326483948df7)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/aa27c768d87f7e2fa11a8b3775f91e7178f245e9)
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/232d5099575c9f7a4ea77c8d7546799ca7ab113d)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/06d96170862eb92c58b1e0116a5e45d5d212eeb0)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/5c039435dd4ccc94ebe7e58a167c31e4dd68d4e6)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/2fa7266b4e417205864edf445346805da82c128f)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/d8d3b6e07eb7807e91dea1e667eb768a2eb3c10a)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8563c18280b4c4d91657b48d7444cde23660f44d)
- Updated Ukrainian translation using Weblate by @Serega124 in [commit](https://github.com/spacecowboy/feeder/commit/1362067e2d66cdfe4757bcb7a4e857371e204a87)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9409eb87e104102939d806bdcc0f205740f1db01)
- Updated Swedish translation using Weblate by @bittin in [#187](https://github.com/spacecowboy/feeder/pull/187) 

### ❤️  New Contributors
* @Serega124 made their first contribution
* @zoli111 made their first contribution in [#184](https://github.com/spacecowboy/feeder/pull/184)

## [2.6.14] - 2024-01-04

### 🐛 Bug Fixes & Minor Changes
- Fixed mark as read on scroll also marking items when opening items by @spacecowboy in [#177](https://github.com/spacecowboy/feeder/pull/177) 

### 🌐 Translations
- Updated Bulgarian translation using Weblate by @trunars in [#174](https://github.com/spacecowboy/feeder/pull/174) 


## [2.6.13] - 2023-12-22

### 🐛 Bug Fixes & Minor Changes
- Fixed release pipeline by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4e2791df22159c1bd82c1e9919a202dadb937d88)
- Fixed mark as read on scroll by @spacecowboy in [#158](https://github.com/spacecowboy/feeder/pull/158) 
- Fixed small thumbnails not being displayed in card style by @spacecowboy in [#163](https://github.com/spacecowboy/feeder/pull/163) 
- Fixed crash for zero width images by @spacecowboy in [#164](https://github.com/spacecowboy/feeder/pull/164) 

### 🌐 Translations
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/e5f41f6a820b5958ad91985598a9fb41ec13d13c)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b1c004f71eb4354445889453af62f5b86100934b)
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/2d737ef287fcd90a05aadfcbfd953316da9175d6)
- Updated Italian translation using Weblate by @Fs00 in [commit](https://github.com/spacecowboy/feeder/commit/0de8be380f45e5d5f4c03be5edfdd0502ee79ca9)
- Updated Chinese (Simplified) translation using Weblate by @ygxbnet in [commit](https://github.com/spacecowboy/feeder/commit/384e2a22c5399c6464738ef054e336ced516395c)
- Updated Albanian translation using Weblate in [#154](https://github.com/spacecowboy/feeder/pull/154) 


## [2.6.12] - 2023-12-17

### 🐛 Bug Fixes & Minor Changes
- Fixed small images being rendered too large and flickering on scroll by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0044d6b1f4f11e58b179695012a3bee5c7e77871)
- Added caching on failed (4xx) network requests by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3ec908d84d328def114b21e94450ca454589b88b)
- Changed so tablets don't force images to be 16:9 anymore by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/aa2dfc699e25d1ae7d743e402764df83f0b71961)
- Added debugMini icon and app name by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e7484738124dd38261f5a978222cd22171b665da)
- Improved scroll performance by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2fa239a41c6da1b05d1a351065a294c722afc421)
- Added support for data-img-url in images by @spacecowboy in [#147](https://github.com/spacecowboy/feeder/pull/147) 
- Removed debugMini configuration by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e936b2bc2759cfe9e8bfd2de91ccfa841f289e07)
- Added emulator tests to release script by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4de81e11c0b6d8d8f46653c70d7b0d0d8b376bf1)
- Removed Toki Pona Play store translation: it is not supported by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/923df8ac03af2398b444a7a1fcf2e9b65c53d23f)

### 🌐 Translations
- Translated using Weblate (Toki Pona) in [commit](https://github.com/spacecowboy/feeder/commit/4a65092552e753e02262b149e63bff416d33dfbd)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/a3b7569689295f79d44d6cba2fe73d8ef9676fef)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/a804a9eb7a62d6d27ee1af04f7805ce5230c6b00)
- Updated Vietnamese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e87046b478b4150727e90e3c52770fc9b9d7064a)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8df6f688ecc0d339db7a27f81a09daa5c153d110)
- Updated Hungarian translation using Weblate by @mdvhimself in [commit](https://github.com/spacecowboy/feeder/commit/707db914460a6e644c3f471305264c5d17c89803)
- Updated Catalan translation using Weblate by @sf0nt in [commit](https://github.com/spacecowboy/feeder/commit/ef850fa10ca879cdb1f7e37181019e7728ec1d80)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b453bb590d6a7adcf308770a765f01600d25c38d)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/bcd36c36f57ad119f3494d49954cc93bb7611e66)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/f7c2c9f7751abbaa88f0f3dce45a39177ea19851)
- Updated Dutch translation using Weblate by @mm4c in [#152](https://github.com/spacecowboy/feeder/pull/152) 


## [2.6.11] - 2023-12-11

### 🐛 Bug Fixes & Minor Changes
- Fixed items getting stuck when swiping them away by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5dcc336f54a4b0847716834cdb7ab4c26dba0dbf)
- Fixed missing files for eu-ES by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/784bcd3488ce08295008f4316f819cfe2773bbb1)

### 🌐 Translations
- Updated Swedish translation using Weblate by @bittin in [commit](https://github.com/spacecowboy/feeder/commit/0658b4743872568f371614e04d58c7d759cdff5b)
- Updated Esperanto translation using Weblate by @felixity1917 in [commit](https://github.com/spacecowboy/feeder/commit/b64609b5d6635d6ad852c7c14820e2c3da264d42)
- Updated Greek translation using Weblate by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/9f09243536eae748e7b93ea3677f7877949b94ca)
- Updated Turkish translation using Weblate by @MehdiKurtcebe in [commit](https://github.com/spacecowboy/feeder/commit/d230ff05c0e2524dc2859676a811a46650b60f82)
- Translated using Weblate (Basque) in [commit](https://github.com/spacecowboy/feeder/commit/a843129627d0b60218150c040204cadd9c0d6341)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [#150](https://github.com/spacecowboy/feeder/pull/150) 

### ❤️  New Contributors
* @MehdiKurtcebe made their first contribution

## [2.6.10] - 2023-12-04

### 🐛 Bug Fixes & Minor Changes
- Fixed crash when a resource string was not styled as expected by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e89df3badb7954e0b5b6d15b1a02523c5333e4e3)
- Added reading time/word count for languages which use spaces by @spacecowboy in [#134](https://github.com/spacecowboy/feeder/pull/134) 
- Added kurdish language file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d1d7b1a5e1b010a0b531f2bd7bcdaf6ec31df273)
- Changed word/minutes to plural strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8433be8d50e96da44d4cfe22b79a6840a630c7f0)
- Updated to ktlint 1.0.1 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/63e5e58457c7be5d066f3e9a928bc1d64e801ce4)
- Formatted according to latest ktlint by @spacecowboy in [#140](https://github.com/spacecowboy/feeder/pull/140) 
- Added leading zero to seconds formatting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/12bd7ccf6a873c874b742caaebf554f62b0919c0)
- Fixed elements hidden by CSS being displayed by @spacecowboy in [#146](https://github.com/spacecowboy/feeder/pull/146) 
- Added display of article image inside reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4efc9d706526fc5acf800398a5774a020ffae46e)
- Fixed image captions appearing twice in full text articles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/42533b326e090bd6f125df55ba46073c869d30bf)
- Fixed ktlint triggering on compose function names by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/833c51fa8933a49e91e950e4c29f85fc0baf62af)
- Fixed some cover images appearing twice in reader if full text by @spacecowboy in [#139](https://github.com/spacecowboy/feeder/pull/139) 

### 🌐 Translations
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/55d2a796b3d9be48da1eb7d23a7afb243ab4d2fa)
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0c4e4ca961328a6a6ae2eeb65a252785b4a875ff)
- Updated Romanian translation using Weblate by @simonaiacob in [#128](https://github.com/spacecowboy/feeder/pull/128) 
- Updated Esperanto translation using Weblate by @felixity1917 in [#132](https://github.com/spacecowboy/feeder/pull/132) 
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/d06e83e1f82184f4bc575b630d82e0db4a381587)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8f3b03f56cb4921a870e9462e8fdf31523d74c61)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/1bedec878f33688cad5608fc3e040b9f71de9c86)
- Updated translation files by @weblate in [commit](https://github.com/spacecowboy/feeder/commit/7dc10cedbac2512ac2b74ad0ecb287b8d21a3ae4)
- Updated Czech translation using Weblate by @miraficus in [commit](https://github.com/spacecowboy/feeder/commit/1760fe94d082c0aacc875e35425723f49208f642)
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/c7a535c6f2b813e09d8c6f5e9de950859b21ecf3)
- Updated Kurdish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/78ddcc5ced50d2144dcd08b70591261add0a2f7c)
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b4c15b92d3fbf7883364ba4c8366929effbcbdaf)
- Updated Japanese translation using Weblate in [#138](https://github.com/spacecowboy/feeder/pull/138) 

### ❤️  New Contributors
* @felixity1917 made their first contribution in [#132](https://github.com/spacecowboy/feeder/pull/132)

## [2.6.9] - 2023-11-18

### 🐛 Bug Fixes & Minor Changes
- Fixed crash when table had no columns by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2bdade903e151d4fcebaaa7172d7ce6b3fcd0515)
- Fixed crash when trying to TTS play a missing file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/03924c15f62143d834861ea62a5e6accbd1f0fbc)
- Fixed another crash in table rendering by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cda338b75eee4d1c8deeede168b5420aaf51b21f)
- Fixed crash if trying to notify for too many items by @spacecowboy in [#121](https://github.com/spacecowboy/feeder/pull/121) 
- Changed so image enclosures are shown in the Reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f1a3a237b2e21fd42e715863cc50e6794e9e6716)
- Fixed so list items are not immediately given newlines if followed by paragraph by @spacecowboy in [#123](https://github.com/spacecowboy/feeder/pull/123) 
- Moved all dependencies into bundles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/020c31e737bbb89398a4e0ae89f82d68b1a5fd44)
- Changed to ksp and upped kotlin and compose compile by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ce8461c129f03d380f8491f737335a074b5745a7)
- Removed bad language by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/89775af8257416953ff6b3b12a705329501264f8)

### 🌐 Translations
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b5f4ca49b755b90488cc1fc2148d71f1d423f844)
- Translated using Weblate (Portuguese) by @vitorhcl in [#117](https://github.com/spacecowboy/feeder/pull/117) 


## [2.6.8] - 2023-11-09

### 🌐 Translations
- Updated Spanish translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/c48624de1e46eddeb7cb850f188683a4e395b366)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b70dd04999bfbabab31323b4928111b2da2a0601)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/2ffda1a65abb17bef1322066a75b7a825d701a8f)
- Updated Vietnamese translation using Weblate by @ngocanhtve in [commit](https://github.com/spacecowboy/feeder/commit/5d76fbeb58b273bed903c4297c0d4f7252e81b48)
- Updated Greek translation using Weblate by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/091dbaa4d24ab0b99e3784af0baa86394e0c137d)
- Updated Italian translation using Weblate in [#113](https://github.com/spacecowboy/feeder/pull/113) 
- Updated French translation using Weblate by @Matth7878 in [#114](https://github.com/spacecowboy/feeder/pull/114) 
- Updated Czech translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d71d728bbc0c5b7c5f7a8e224bc113beb1b0fdb8)
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/7ddadba46ebeacd49154a233927f9814521c0812)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/ce281b4642c38c971f2379bca4dfba3cd4c78b75)
- Updated Dutch translation using Weblate by @mm4c in [#115](https://github.com/spacecowboy/feeder/pull/115) 
- Updated Indonesian translation using Weblate by @zmni in [#116](https://github.com/spacecowboy/feeder/pull/116) 


## [2.6.7-1] - 2023-10-23

### 🐛 Bug Fixes & Minor Changes
- Added missing title because weblate/fastlane sucks by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4570b2a6c1a38b90840e71ddeee3ee1aa423d785)


## [2.6.7] - 2023-10-23

### 🐛 Bug Fixes & Minor Changes
- Added option to open links in an adjacent window on large screens by @spacecowboy in [#110](https://github.com/spacecowboy/feeder/pull/110) 
- Fixed notifications not following settings for item opening (#108) by @derdilla in [#108](https://github.com/spacecowboy/feeder/pull/108) 
- Added max age of cached responses with at most 15 mins unless manual sync by @spacecowboy in [#112](https://github.com/spacecowboy/feeder/pull/112) 

### 🌐 Translations
- Updated Romanian translation using Weblate by @mozartro in [commit](https://github.com/spacecowboy/feeder/commit/df81e4022791f7e4c3787213058673ba40f5f333)
- Updated Telugu translation using Weblate by @Harsha0431 in [commit](https://github.com/spacecowboy/feeder/commit/b23fd0b6d77c73d56bfd0b4921bf3270be2d9087)
- Updated Portuguese (Brazil) translation using Weblate in [#106](https://github.com/spacecowboy/feeder/pull/106) 
- Translated using Weblate (Bulgarian) by @trunars in [#107](https://github.com/spacecowboy/feeder/pull/107) 
- Updated Vietnamese translation using Weblate by @ngocanhtve in [commit](https://github.com/spacecowboy/feeder/commit/f9109ce428a2e4f3c86a6353b510c5d97f2d73cc)
- Added Toki Pona translation using Weblate by @weblate in [commit](https://github.com/spacecowboy/feeder/commit/a05e7974784c1d720eac7b01d606ad66f8e30c7e)
- Deleted Afrikaans translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/36756641b821114c57bc627b867f9a65672b7c0f)
- Deleted Arabic (Saudi Arabia) translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/374ab9880c450662d163c6b4bf24524c2e18f5b0)
- Deleted Basque translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a875b674ab283951ff18687e1ad28b9fac99a6a1)
- Deleted Galician translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f2e05b61db946c322d310326878420648727cae4)
- Deleted Hebrew translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9fd8774ea5c6eeeeda111d8882538f82f24a7664)
- Deleted Hebrew (Israel) translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ef6d15635e9afe065eac4b2087c5fb3dc5a05084)
- Deleted Korean translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/548a570b003b255e7ed6168f852d0034c00333dd)
- Deleted Portuguese translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9550277aa330f098924daa5cff3a8d3d27273120)
- Deleted Toki Pona translation using Weblate by @spacecowboy in [#109](https://github.com/spacecowboy/feeder/pull/109) 

### ❤️  New Contributors
* @derdilla made their first contribution in [#108](https://github.com/spacecowboy/feeder/pull/108)
* @ngocanhtve made their first contribution
* @trunars made their first contribution in [#107](https://github.com/spacecowboy/feeder/pull/107)
* @Harsha0431 made their first contribution
* @mozartro made their first contribution

## [2.6.6] - 2023-10-08

### 🌐 Translations
- Updated Thai translation using Weblate by @bowornsin in [#104](https://github.com/spacecowboy/feeder/pull/104) 


## [2.6.5] - 2023-09-30

### 🐛 Bug Fixes & Minor Changes
- Added ability to force add a feed despite network errors by @spacecowboy in [#102](https://github.com/spacecowboy/feeder/pull/102) 

### 🌐 Translations
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/5a4c5c7b7f37bc44acf6c29f44efbb5f19134593)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/634379b04261091b8b318ba1ce078d0444747cc0)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/5fa3ac9eaf1ec70523b56cedf7249617aec31b76)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/88fa51db975ed8a416dc667742c178530763cbce)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9c6fe4a138a14fb4ecced0fb01261efb965ebdea)
- Updated Greek translation using Weblate by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/5cde461879e3529284a0f6b161548f80c83ee4a5)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/1f7ede072a5c42bc6c451c037f4e21395976221e)
- Updated Serbian translation using Weblate by @eevan78 in [#103](https://github.com/spacecowboy/feeder/pull/103) 


## [2.6.4] - 2023-09-23

### 🐛 Bug Fixes & Minor Changes
- Improved OPML import when files are incorrect by @spacecowboy in [#100](https://github.com/spacecowboy/feeder/pull/100) 
- Some gradle housekeeping by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2f564e6258981725444ae7af48fb660a86a8de50)
- Fixed crash: Parcel: unable to marshal value HttpError by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/369a17d4452811a92a21d577169b6f9c37aa69ee)
- Fixed crash: URLDecoder: Illegal hex characters in escape by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/434ecae8335ee2055dfbbe76b84c00de78bcea07)
- Fixed crash: gzip finished without exhausting source by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f258c45ece1d53a97e65a9d21cdeb77e97ce8385)
- Fixed crash: file:/// exposed beyond app through ClipData.Item.getUri() by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/477e3b328b22a6d9834cf08a905d2b3f7544bed7)
- Fixed crash: No Activity found to handle Intent { act=android.intent.action.CREATE_DOCUMENT by @spacecowboy in [#101](https://github.com/spacecowboy/feeder/pull/101) 

### 🌐 Translations
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/994c9904cd9c62b79c7577019acdd73f76929587)
- Updated Indonesian translation using Weblate by @zmni in [#99](https://github.com/spacecowboy/feeder/pull/99) 


## [2.6.3] - 2023-09-12

### 🐛 Bug Fixes & Minor Changes
- Changed so sync will try to fetch the favicon of a site if no feed by @spacecowboy in [#97](https://github.com/spacecowboy/feeder/pull/97) 
- Made builds reproducible by @spacecowboy in [#98](https://github.com/spacecowboy/feeder/pull/98) 

### 🌐 Translations
- Updated Chinese (Simplified) translation using Weblate by @ygxbnet in [#95](https://github.com/spacecowboy/feeder/pull/95) 


## [2.6.2] - 2023-09-10

### 🐛 Bug Fixes & Minor Changes
- Changed so sync uses only a single CPU-core by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1195f0cc227b5819f47937a8db039c35751d4675)
- Improved some error handling by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/49a7f6538da965c5aa0cfc34961d87cc39d027aa)
- Fixed crash in edit feed text related to focus by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ce759df217bfd500c101dde7b6053e85ad84c488)
- Made the OPML importer tolerant of ill-formed XML (bad files) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/91c0627e548b5ec1b02d61da6a2251652e3426e0)
- Fixed text in list possibly getting out of date with data by @spacecowboy in [#93](https://github.com/spacecowboy/feeder/pull/93) 
- Added support for more types of feed icons by @spacecowboy in [#94](https://github.com/spacecowboy/feeder/pull/94) 

### 🌐 Translations
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/ec5281aa6ca3c8d197b5d209804958873c7d15d1)
- Updated Chinese (Simplified) translation using Weblate by @ygxbnet in [#92](https://github.com/spacecowboy/feeder/pull/92) 

### ❤️  New Contributors
* @ygxbnet made their first contribution in [#92](https://github.com/spacecowboy/feeder/pull/92)

## [2.6.1] - 2023-09-04

### 🐛 Bug Fixes & Minor Changes
- Fixed a crash if device was removed from sync chain by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/082954271611df32082fe7a5c26bb5027c84527d)
- Implemented Either from Arrow by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7f4ec4f65077d95d0cce077f7250e5424f247c28)
- Added more descriptive error messages when feeds can not be found or parsed by @spacecowboy in [#85](https://github.com/spacecowboy/feeder/pull/85) 
- Added a new theme specifically for E Ink screens by @spacecowboy in [#89](https://github.com/spacecowboy/feeder/pull/89) 
- Added some extra crash handling by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/171477fa816559242910c901b72d7eb431237d7b)

### 🌐 Translations
- Updated Catalan translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/1695a4b76faa4a8183cb27e4206647240bc72bae)
- Updated Czech translation using Weblate by @pchelium in [commit](https://github.com/spacecowboy/feeder/commit/a0bd0cc6ab6889813264f638359930f00f4bf92b)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/14284127544637d1f3bb60f91be36a0ec2ada9f8)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/a7cb24d25a8c956b1a2ef65a436c9693b6d80670)
- Updated Dutch translation using Weblate by @Mustachipleb in [commit](https://github.com/spacecowboy/feeder/commit/42feb9e13ceeeb630284d00039974b6b2426c8dc)
- Updated Arabic translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8def65df71db6935a5d7ca8eabdfb2bc5c3c9ff4)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/5e817c63e60671d4150ddbe4c73219305329dfad)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/e75a810dd8d58bc742c313d61ecc2c4d98e88056)
- Updated Japanese translation using Weblate by @larouxn in [commit](https://github.com/spacecowboy/feeder/commit/5be51a611fc18c4d46f14f6906f9e9ae3e7ed55b)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/49505e90d2b12c6b393843a937e3a96b8347b8f5)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/7ad678a3e618b62a8750f31d084c52091172a6ab)
- Updated Greek translation using Weblate by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/e51efd2798f3f06bd30611a713f69123cae62774)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/1db118d5c2b16ad6aad316cc3d678bc22b54672a)
- Updated Chinese (Simplified) translation using Weblate by @xlucn in [#86](https://github.com/spacecowboy/feeder/pull/86) 
- Updated Portuguese (Brazil) translation using Weblate by @edxkl in [commit](https://github.com/spacecowboy/feeder/commit/07dadfa5bd24855a9a15253a772ab86b0db07623)
- Updated Serbian translation using Weblate by @eevan78 in [commit](https://github.com/spacecowboy/feeder/commit/f1b655603c42e37a61502516dbbd1c91b8fd9c90)
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/ef1222ddd0760f7ce0b6bcd58c8f6b1fb8597035)
- Updated Ukrainian translation using Weblate by @Kefir2105 in [#87](https://github.com/spacecowboy/feeder/pull/87) 
- Updated Finnish translation using Weblate by @jkinnunen in [#88](https://github.com/spacecowboy/feeder/pull/88) 
- Updated Italian translation using Weblate by @amelillo in [#90](https://github.com/spacecowboy/feeder/pull/90) 
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/f72747797fcfcc5a94c9a3834c3f8d1fdacd7763)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/93aae0047f747cfc4cbd86242d0f1e80ec57f0dc)
- Updated Greek translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b4d0c81e25ff4060e70b302895b729b4865abe4b)
- Updated Slovak translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/75b785a1fa3801915e2e2b8b2720b3f39a4a17f9)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/ec8094fc2cfeb71940ea0e944aa05de7d701090d)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/acb40347119a81c78415de96fe1c72d91e6bd8c9)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/c436131468034826bdc00fd3acbc4f597e76f96d)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9caae62d18323cbd787c21ea36a637cb2e9c9d2e)
- Updated Hungarian translation using Weblate by @Kingproone in [#91](https://github.com/spacecowboy/feeder/pull/91) 

### ❤️  New Contributors
* @amelillo made their first contribution in [#90](https://github.com/spacecowboy/feeder/pull/90)
* @jkinnunen made their first contribution in [#88](https://github.com/spacecowboy/feeder/pull/88)
* @edxkl made their first contribution
* @xlucn made their first contribution in [#86](https://github.com/spacecowboy/feeder/pull/86)
* @larouxn made their first contribution
* @Mustachipleb made their first contribution

## [2.6.0] - 2023-08-14

### 🐛 Bug Fixes & Minor Changes
- Changed so navigation drawer now remembers scroll position by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1bbaa426fcc62b968e361d1a5756dab9314d053f)
- Fixed deleting current feed will switch to All Feeds view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7454a239bf02dcfb358bc049e9b8bb8b90885520)
- Changed so the Mark All as Read button (FAB) will also immediately hide all items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/80f209e1bf761efeb8a8b16c7cf1240552c27cba)
- Added new option to configure max lines for items in list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/61b79f4b1ede892fd5657e3bdd5450535a26217f)
- Changed Compact article style to match other styles and take advantage of configurable max lines by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4800309f9ea08e7c7d491e7764aa62422582d195)
- Improved image loading in reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4472d2c956b3219b2deb02486edf415e714d3762)
- Added new filter options by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/468d953a76e932c12058a3907c657e83de0c3948)
- Increased icon size in SuperCompact style by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/98c2a504e74a7dcca41d2c8d66ea6d0379189e4f)
- Renamed some string names to make it clear if they are adjectives or nouns during translation by @spacecowboy in [#81](https://github.com/spacecowboy/feeder/pull/81) 
- Fixed talkback on new filter menu by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c426488c9b1879c8c68d3a20ce97acb85294208e)
- Added new setting for limiting list items to only title or not by @spacecowboy in [#82](https://github.com/spacecowboy/feeder/pull/82) 
- Fixed crash with device sync (#84) by @spacecowboy in [#84](https://github.com/spacecowboy/feeder/pull/84) 

### 🌐 Translations
- Updated Czech translation using Weblate (#80) by @weblate in [#80](https://github.com/spacecowboy/feeder/pull/80) 
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/437f5af8bb4b384ad752a2019e2718d75d545816)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/3ae0d2ab036c74fc9eb6f4fad2cc495e729c5af4)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/052c6e483c19413b25e7bb6294522c5139d1075b)
- Updated Ukrainian translation using Weblate by @Kefir2105 in [commit](https://github.com/spacecowboy/feeder/commit/c736b141cc7de2a8b3f490fc6283876efc0bdd9f)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/651c9ad594435c975c0443b8d15f8334354e837a)
- Updated French translation using Weblate by @Matth7878 in [commit](https://github.com/spacecowboy/feeder/commit/1c4d1d717379f001e133c3feedce1e737b74f45b)
- Updated Italian translation using Weblate by @Fs00 in [commit](https://github.com/spacecowboy/feeder/commit/9422a197efb2e8c408631263073afe016f66122a)
- Updated Chinese (Simplified) translation using Weblate in [#83](https://github.com/spacecowboy/feeder/pull/83) 

### ❤️  New Contributors
* @Matth7878 made their first contribution

## [2.5.0] - 2023-08-09

### 🐛 Bug Fixes & Minor Changes
- Added handler for crashes: will open a pre-formatted email to report the crash by @spacecowboy in [#72](https://github.com/spacecowboy/feeder/pull/72) 
- Added automatic releases on GitHub with easily downloadable APK-files by @spacecowboy in [#75](https://github.com/spacecowboy/feeder/pull/75) 
- Fixed bug where you could get stuck in Couldn't Fetch Full Article Text by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3e3b93f015119652a5515d326ea26ebbfd4e90b9)
- Fixed so fetch full text will only try to read HTML links. PDFs and similar will result in an error text by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b073476959e1732ad5bbadbcfcbe515f4575f61c)
- Fixed possible crash if OMPL import failed by @spacecowboy in [#76](https://github.com/spacecowboy/feeder/pull/76) 
- Ensured non-html links are not downloaded in vain when parsing full articles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7f78acbe3944c3595dd1d6b91b69e75576fadba9)
- Upgraded dependency versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2a11651afbf77e409b2d4d185cfe7397b7aff62a)
- Changed so articles don't disappear immediately when they are read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6e629e1bd21c57dacbbe0a1d05276c20a69676e5)
- Changed so articles published today show time instead of date by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fbb37288d2e98c480d10a7d9191d0fd0bb1ed3bf)
- Updated article styles: increased difference between read/unread, improved usabilty in phone landscape orientation, added feed icon to card by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9dd52cd297a126e8a8da5281e7327f6e0da93912)
- Re-enabled mark as read on scroll for grid; Card style is now the only option for the grid layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b0565a361f86fd4e75cb2bfeca8422cce47de060)
- Changed ContentProvider to build type specific values by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/253d153900d9d45afb815e9f7de9f92afdcfc8fb)
- Changed so swiping will hide article immediately if only showing unread items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/50e924e916d95f2a77fd4366d7596e7b080b9e26)
- Added Feeder specific settings to OPML by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/72782af94e8048c392a15574eb71e93195a97b37)
- Added a shared key for debug builds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e456f2435cc3ae17c7e6dc47e664a73f02ba337f)
- Upgraded from threeten.bp to core library desugaring for java.time by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8249d95ca74914060335769f102f8e9877385f15)
- Fixed tests to run on Android 23 by @spacecowboy in [#78](https://github.com/spacecowboy/feeder/pull/78) 
- Fixed Hungarian name in Fastlane metadata by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/32fe6403ef7f3294eb12df2026acce1f5725ec5c)

### 🌐 Translations
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/dfc70bd39ea254edf555b868851fd2955dc0bbfd)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/2778cf14ecc1d7d35513abbf9b55e827e9d44ea9)
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3f0a9cb4aa29faad8ad07746c7dd5b17b90a1a20)
- Updated Italian translation using Weblate by @atilluF in [commit](https://github.com/spacecowboy/feeder/commit/f5594a930440346d18ba326b25a331ce76590915)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/7c0247f4d246cf19fce7dacfdbbbc3ac771d3e23)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/0818b4a7a9a14db9009820a4f1480dcf86369399)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/22acf1f1766d19a8cf34c5e4a6379cd92c9a4e19)
- Updated Swedish translation using Weblate by @bittin in [commit](https://github.com/spacecowboy/feeder/commit/34f3afd37c9e5a3f6f84a3573aea64486aaa83c8)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e2828a30af4bdd81d7863ab711b9453515b90f34)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e7b81bb9de95f21e3ccd0a87c38de8cce87c715e)
- Updated Portuguese (Portugal) translation using Weblate by @SantosSi in [commit](https://github.com/spacecowboy/feeder/commit/f8e72164b2da20185aa9da6100d7cdd997753658)
- Updated Greek translation using Weblate by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/dbbe60f16f612fc06c90957a55c89127e7fc592a)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4fa7e6d0ae912c71b9b429deb79e96b79e84711c)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a66281a7a71e1c89019539d7a5e185adf135ecfe)
- Updated Ukrainian translation using Weblate by @Kefir2105 in [commit](https://github.com/spacecowboy/feeder/commit/2f1271feecc1e2d8de5837ba28392b17505c4ae7)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/fee820a70f397efcf8f3e3e16e4c22f9d944cf26)
- Updated Italian translation using Weblate by @Fs00 in [commit](https://github.com/spacecowboy/feeder/commit/a03f8fdb3b8c6b64c70f3aab6130dd92456d9087)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/85503868fee2800fe8a1b19d174a036454478c45)
- Updated Hungarian translation using Weblate by @Kingproone in [commit](https://github.com/spacecowboy/feeder/commit/d4856de4f63657a687faf9cfd30f796c927c2d37)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/c16773afd4d974b4e9bd65ec72226a2bd2700832)
- Updated Swedish translation using Weblate by @spacecowboy in [#77](https://github.com/spacecowboy/feeder/pull/77) 
- Updated Indonesian translation using Weblate by @rezaalmanda in [commit](https://github.com/spacecowboy/feeder/commit/6de1e084ea005b3937ce17f0d02ae22aa3f256a6)
- Updated Dutch translation using Weblate by @mm4c in [#79](https://github.com/spacecowboy/feeder/pull/79) 

### ❤️  New Contributors
* @Kingproone made their first contribution

## [2.4.20] - 2023-07-19

### 🐛 Bug Fixes & Minor Changes
- Added global text actions to text selection menu by @spacecowboy in [#73](https://github.com/spacecowboy/feeder/pull/73) 
- Fixed bug where 1x1 tracking pixels could be selected as cover images by @spacecowboy in [#74](https://github.com/spacecowboy/feeder/pull/74) 

### 🌐 Translations
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/7c9ea4d7c0246ce89346519f0b49e70ae0440445)
- Updated Italian translation using Weblate by @nicola-masarone in [commit](https://github.com/spacecowboy/feeder/commit/f2ad8249730115c930ef717070251802d10cc156)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/30e88bc11b6a4ec2bb5dfd34ea33bd0b96da8cef)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/9ab7037e2b3ab6eb607468558c297783a0ce9632)
- Updated Japanese translation using Weblate in [#71](https://github.com/spacecowboy/feeder/pull/71) 


## [2.4.19] - 2023-06-25

### 🐛 Bug Fixes & Minor Changes
- Fixed article ending up in a mixture of full article and regular by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/794e0928616595c2d17fab7b87509b2c8b13b16d)
- Fixed full text download worker to be slightly more optimal by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4350b21d1e11f17805c0bca493cdb4065276b34e)
- Tweaked requests' Cache Control headers by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9cad034f98367a85a11527e1329d3bce307dd4fb)
- Fixed sites getting fetched on every sync if they didn't specify an icon by @spacecowboy in [#69](https://github.com/spacecowboy/feeder/pull/69) 

### 🌐 Translations
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/29affd7e6ccc751122e9bebad1de455d3fdd36d3)
- Updated Italian translation using Weblate by @atilluF in [commit](https://github.com/spacecowboy/feeder/commit/05dd33b7a72971b1a4d3a6994e7ec1ca684e5156)
- Updated Greek translation using Weblate by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/0c626ff8fd8929b3bf99db180f1c01f987734104)
- Updated Czech translation using Weblate by @pchelium in [commit](https://github.com/spacecowboy/feeder/commit/491e05454cb9d6fa66f3706e98e5d74b8635c074)
- Updated Portuguese (Portugal) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9a6ec389ea79fed5f55ccf2d8eeffb94df5e711d)
- Updated Thai translation using Weblate by @bowornsin in [#68](https://github.com/spacecowboy/feeder/pull/68) 


## [2.4.18] - 2023-06-19

### 🐛 Bug Fixes & Minor Changes
- Added ability to open OPML files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dff22b6588dd314ba728f83634d74376003b7afc)
- Added Feeder News feed for all users. So sorry for modifying everyone's subscriptions! It only happens once for each user. Feel free to delete it if you don't want it. by @spacecowboy in [#67](https://github.com/spacecowboy/feeder/pull/67) 
- Fixed possible crash when inserting duplicate feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/06e27ca6f59d083f6801f83347b449b106227b89)

### 🌐 Translations
- Updated Basque translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/72cd7faba4b17b26bf552403bd04eec48ad2b947)
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0b71640c3b09b028eac9202d5dea7b1f9c7d6c53)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d4ad56239b93fe71231a2f2d40f78b2075ac1cb1)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/f203fdaa3b2cf17371fe5cea8574ff18c7ed0dd6)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/565de63cfef55e582f8b891b8061a8a6f3541628)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/31a7aee8b96bdcd7b1a791fbb7069cd326a20d9d)
- Updated Ukrainian translation using Weblate by @Kefir2105 in [commit](https://github.com/spacecowboy/feeder/commit/4d7b2ac61e19d8dbbd6c3de3220510058937d58c)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d81a686becf32ad35fec0fc91f1d4f8ca26368ed)
- Updated German translation using Weblate by @wolmoe in [commit](https://github.com/spacecowboy/feeder/commit/4fcf1af253b77656de254b6fc2aa881eadef60dc)
- Updated Swedish translation using Weblate by @spacecowboy in [#66](https://github.com/spacecowboy/feeder/pull/66) 

### ❤️  New Contributors
* @wolmoe made their first contribution

## [2.4.17] - 2023-06-09

### 🐛 Bug Fixes & Minor Changes
- Fixed possible crash during article parsing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/71835f812e7de25ec84d34a855a42cdfa1920b3a)
- Fixed charset detection for sites not using UTF-8 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/36f08f92ac9a49a416739400894044183390bd99)
- Fixed lineheight not scaling with text size by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4b2c4bd355b05a14c025af9fecbf12a6d4b4fc3f)
- Improved keyboard navigation through the app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f3d1b0f5904614ed77e2bea7d4825796f2141d9e)
- Added suitable dimensions for TVs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dbd9af1608a83e7be4b12aa519a1a01c9cb61820)
- Changed how number of columns in grid layout is calculated by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b7501f87c67265cc9352853981eb760cbdc4f372)
- Added some special UI handling for Foldable devices by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/032b77aeb3c0438a9dbd47fc9dd1e4b13beb15b2)
- Adjusted width of reader on large screens by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c0578304af16870910fe8946f299cf8041a3ae0a)
- Fixed some code deprecations and such by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/967797ddc33a80ad2123eb627b62a48d60d1f83c)
- Temporarily disabled mark as read on scroll in Grid because it just doesn't work well enough by @spacecowboy in [#64](https://github.com/spacecowboy/feeder/pull/64) 

### 🌐 Translations
- Updated Dutch translation using Weblate by @mm4c in [#63](https://github.com/spacecowboy/feeder/pull/63) 
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/ea412aa25006c503ac66ea5ddf81f4a951262782)
- Updated Finnish translation using Weblate in [#65](https://github.com/spacecowboy/feeder/pull/65) 


## [2.4.16] - 2023-06-06

### 🐛 Bug Fixes & Minor Changes
- Added global notifications setting as an alternative way to toggle feed notifications by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3493e09b9b72d083da0e881ee18af01e273cdd4e)
- Changed block list setting to have dynamic size by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6d11e1658eb319067c2948b8dc6d54829f558acf)
- Cleaned up some code by @spacecowboy in [#55](https://github.com/spacecowboy/feeder/pull/55) 
- Added an entry in the nav drawer to easily access bookmarked articles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cce0be9ba1468d18c8c8fb4c0ab96720fd248399)
- Fixed mark as read after/before by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fd0577d4dd47440e09d8b6ad7b4fe8ae711c8c9a)
- Removed ability to pin articles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/502c16cd6a7991a8ef82b334a94d34db119c19a8)
- Renamed "bookmark" to "save article" by @spacecowboy in [#54](https://github.com/spacecowboy/feeder/pull/54) 
- Ensured old pinned articles becomes saved articles when upgrading by @spacecowboy in [#56](https://github.com/spacecowboy/feeder/pull/56) 
- Changed so Feeder will try and parse responses from sites even if the mimetype is wrong by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e390d2296bb92f59d03c33c245fcb9a00fb0b972)
- Added option to mark as read while scrolling by @spacecowboy in [#57](https://github.com/spacecowboy/feeder/pull/57) 
- Changed mark as read on scroll delay to 800ms down from 1000ms by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/835329dcb9e3873051742d615efa77c56aff3c20)

### 🌐 Translations
- Updated Indonesian translation using Weblate by @zmni in [#52](https://github.com/spacecowboy/feeder/pull/52) 
- Updated Czech translation using Weblate by @pchelium in [#53](https://github.com/spacecowboy/feeder/pull/53) 
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/18901a754e936715c5071c36bb3a52112c0e8da7)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/b62198f8a14228eb19c3fb284057d09ecfa079a4)
- Updated Japanese translation using Weblate by @ROCKTAKEY in [commit](https://github.com/spacecowboy/feeder/commit/fd41e07ce178c8fe7c69f687bd0653c56dcd47cd)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/0368c1ed1b9947fe75e7106b5a3eed0117482cef)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/f4bee166d0d690848a456119639a2a2050bfe840)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8ac1d5f065696f688ea1919692afc7d31be6e7f8)
- Updated Danish translation using Weblate in [#59](https://github.com/spacecowboy/feeder/pull/59) 
- Updated Czech translation using Weblate by @pchelium in [commit](https://github.com/spacecowboy/feeder/commit/6dafba638595d690e8f631e09836e0f47072a1d0)
- Updated Italian translation using Weblate in [#60](https://github.com/spacecowboy/feeder/pull/60) 
- Updated Greek translation using Weblate by @giwrgosmant in [#61](https://github.com/spacecowboy/feeder/pull/61) 
- Updated Czech translation using Weblate by @pchelium in [commit](https://github.com/spacecowboy/feeder/commit/546751ab807bf09e44357a195d2f38daa2aac435)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/c56fb0977b0232b7dab2fba0163736e646479663)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/ae153e5b9715574addd46690c0a5032b404aa1c4)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/cec966ba9f155ada8044c5e3d33b7e47abe91911)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b660084f67ef7b15dba8b1860f8ca24c2b5ae2f6)
- Translated using Weblate (Serbian) by @eevan78 in [commit](https://github.com/spacecowboy/feeder/commit/37648ec44248f7a4b03e86f8e437ab205aaab1d9)
- Updated Ukrainian translation using Weblate by @Kefir2105 in [commit](https://github.com/spacecowboy/feeder/commit/810d0766a5c4ef62d226efa2dc31b95781c0da8e)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/50490d029e2ee4c6766060dcb4fc230a4b9d5ddc)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/647040b62db0080ef7555be6973f38d2aa6e3dd2)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/81db851a2d42905daefb6c62f13e50db08426726)
- Updated Italian translation using Weblate by @nicola-masarone in [commit](https://github.com/spacecowboy/feeder/commit/7807be16de585a0e1d451566af320a86ef8fe3f3)
- Updated Portuguese (Portugal) translation using Weblate in [#62](https://github.com/spacecowboy/feeder/pull/62) 

### ❤️  New Contributors
* @nicola-masarone made their first contribution
* @Kefir2105 made their first contribution
* @pchelium made their first contribution
* @ROCKTAKEY made their first contribution

## [2.4.15] - 2023-05-21

### 🐛 Bug Fixes & Minor Changes
- Updated versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6838e46a6ad7508e915a0001cb81d5757b65ab27)
- Changed to official upsert by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8cd2453a50b171aaa146506e3fb829b1f4e74683)
- Changed to new and safer flow collection with lifecycle awareness by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/aa4c73626cfb7b7837ebac063ba8d72e396d0cae)
- Updated so text should appear more balanced with line breaks and hyphenation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/233afed43509cdd34ee7dbe1400797d505b88c55)
- Added tooltips to all icon buttons on long-press by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5077ad259ff35f6f8024fb387b2028839eae649d)
- Changed so zeros aren't shown in nav drawer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4c7ab11139e81c635572f6627dc52506196f6003)
- Changed so New-indicator is only shown if read items would be shown by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ab30b4527c212d3b766a44517c27653efbd7a8c3)
- Changed so pressing Back will close the nav drawer if it is open by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/48c8b1e08072d1a861e34a5f9762a82b26349cb1)
- Changed placeholder images to be easier on the eyes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/792bdd93b0ed75303c151ba974762ccc00aed08f)
- Fixed too many image captions when image was wrapped in figure by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3c83228ecd825b76c8f95168f441b0a1a5475ca3)
- Changed so image captions are not included in TextToSpeech by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/586d67355da99b019cdea09ed6ef21ea92322ef2)
- Improved article layout with spacing and image captions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6c4f6f14a91e978dbfe76425eac2631df6402d77)
- Improved table rendering in article view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5fd8c8537a6bc112cba1bb10b206b6ae96e80b84)
- Improved reader screen performance by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e23ecb47342d67c950fbd19fa1c3efd3b51cebb8)
- Fixed display of nested figures in reader by @spacecowboy in [#49](https://github.com/spacecowboy/feeder/pull/49) 


## [2.4.14] - 2023-05-10

### 🐛 Bug Fixes & Minor Changes
- Added TW title by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9251378d3f808fe183aef37a39a1a29989ee6855)
- Updated UserAgent to explain what the app is for server owners by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f6bd778eec2973f9541a0ae50cfeee301aec2155)
- Fixed crash when searching for strange URLs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d13def0e9851cdf2a83d3da9c1e995d9003c67af)
- Fixed rare crash in reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/63c0da7a7a041335bf33db7355457a7c5154e169)

### 🌐 Translations
- Updated Odia translation using Weblate by @SubhamJena in [#47](https://github.com/spacecowboy/feeder/pull/47) 
- Translated using Weblate (Chinese (Traditional)) by @yangyangdaji in [#48](https://github.com/spacecowboy/feeder/pull/48) 

### ❤️  New Contributors
* @yangyangdaji made their first contribution in [#48](https://github.com/spacecowboy/feeder/pull/48)
* @SubhamJena made their first contribution in [#47](https://github.com/spacecowboy/feeder/pull/47)

## [2.4.13] - 2023-04-12

### 🐛 Bug Fixes & Minor Changes
- Updated versions and enabled gradle configuration cache by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9f8dd7e40ea6a2e63cc9df7278a85cd1978f4c34)
- Syncing will now scroll list to top so new items are immediately visible by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/34843b8eb068eb1be599ecd6b980cd8e4ba5d7f2)
- Fixed send bug report to open email client instead of GitLab by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3016f72c8d5d26160bc45ef49b54a03da2eccc35)
- Added check for notification permission before trying to notify by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/10c661ad30526250ed4990b52a01a79c5e839a59)
- Improved build performance by @spacecowboy in [#45](https://github.com/spacecowboy/feeder/pull/45) 
- Fixed screen getting offset when increasing display size on device by @spacecowboy in [#46](https://github.com/spacecowboy/feeder/pull/46) 
- Fixed so release script can generate config locales by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cf81e5e5cab17ad4adab45b0cfa20e707390db83)
- Tweaked release script by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2854679f15f0b06367ee83096b7ad5db75946beb)

### 🌐 Translations
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a25e1cbbe9a956544fbf268ed2cac65661a91ef8)


## [2.4.12] - 2023-04-02

### 🐛 Bug Fixes & Minor Changes
- Fixed couldn't add a feed with unknown protocols in links by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/43518971119a4a24fb375d46e3256ff6bcc5a2ad)
- Improved link handling by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c83e7054f9864d08d99cde96b6a55cedca1d29fe)
- Some cleanup by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ca3a977bf15a9e64288946072c402ba7f0823b4a)

### 🌐 Translations
- Updated Malayalam translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/043f5da1a6b9f08014d32a619c37ccaac057479b)
- Updated Dutch translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b502d1ff13933b754b925777278a1f87e21f1e47)
- Translated using Weblate (Arabic) by @zayedalsaidi in [commit](https://github.com/spacecowboy/feeder/commit/95e0e8c06443061a16cacf94c9e38e415534aebb)
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9157af8f4573e3e2bdb5af6bd4bbd924b38aed9b)


## [2.4.11] - 2023-03-26

### 🐛 Bug Fixes & Minor Changes
- Create .github/FUNDING.yml by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bf5132ac104c911d2c77b1137a3b9925c33ccba4)
- Fixed missing translation files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2cf8ec180a9ea2dc989e061946b326c2b336422e)
- Revert "Releasing 2.4.11" by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9bbdcb4b5ee6d35b505b53079c0fbbda02d7384e)
- Fixed missing translation file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2cc8ef5da05581243c940a533823db6ae6f2c56e)

### 🌐 Translations
- Added Slovak translation using Weblate by @LiJu09 in [commit](https://github.com/spacecowboy/feeder/commit/61a263f30250cecfbe3b31025b4e25343b8667b9)
- Translated using Weblate (Arabic) by @zayedalsaidi in [commit](https://github.com/spacecowboy/feeder/commit/3b48b9b84601cf81af58783ca90c5b2de0975007)
- Translated using Weblate (Persian) in [commit](https://github.com/spacecowboy/feeder/commit/1f07f7787113d7b8fbd58581a4d24de50b82b867)

### ❤️  New Contributors
* @zayedalsaidi made their first contribution
* @LiJu09 made their first contribution

## [2.4.10] - 2023-03-11

### 🐛 Bug Fixes & Minor Changes
- Update file README.md by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/265a99c8c5db8ecdb5caad6df392021a5f46030d)
- Disabled emulator tests on github by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c2010a4f4a14a14e494c9fd766794a84c276933a)
- Upgraded kotlin, compose compiler and compose BOM by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/24a024f52f78a849425fe67fd98a71565396f4aa)

### 🌐 Translations
- Translated using Weblate (Japanese) by @S-H-Y-A in [commit](https://github.com/spacecowboy/feeder/commit/6a786c1fb0df03dda5ba50f63fb55b738f047c45)

### ❤️  New Contributors
* @S-H-Y-A made their first contribution

## [2.4.9] - 2023-03-05

### 🐛 Bug Fixes & Minor Changes
- Renamed files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/718af08b077fae03d1f50b37dfed1ba9225e3fa8)
- Fixed lint by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/31c481c10a78d1ab29ab672f920a080098f07400)
- More lint by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d699b44cfc657e953d54a42faab6174418fbbecc)
- Updated guidelines by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4b01751be236e55e5e640dc95a75335840f89b26)
- Updated Android plugin version by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b077ce5c60702422f81c0ebc16f7f7f382e4d6c6)
- Fixed all lint errors by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cfe0511a4922934bf3534b153996a5a0e80bbc49)
- Fixed a deprecation warning by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/036e27411b1ac0fcd0a71c3dd78f7ca512ae4838)

### 🌐 Translations
- Translated using Weblate (German) by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/dbfa1c906e7f0b690e26a7bc7c7b06e88f62f94c)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/6a822bb8cee0101e16b8fa0e972ae4bc210a3e7f)
- Translated using Weblate (Portuguese (Portugal)) in [commit](https://github.com/spacecowboy/feeder/commit/76b2499756efa8fb0ba5730602fd91d1cdfded84)
- Updated Basque translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3d295d48037471a22e2dbc14ac5f7dece7534c77)


## [2.4.8] - 2023-02-16

### 🐛 Bug Fixes & Minor Changes
- Fixed articles marking themselves as unread when toggling view unread by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/736935e516596db76eb8502b95cbd1c7ad60699a)
- Adjust URLs of screenshots by @felix-otto in [commit](https://github.com/spacecowboy/feeder/commit/b14eb75854e30e6fc0bdb1d023432597acbfe7e2)
- Fixed trailing commas by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cac7d5e85b96e1806afbeaa6c6052a881c3a4b9a)

### 🌐 Translations
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/d070511fa797c306733e8c8027dca308587f95f7)
- Updated Catalan translation using Weblate by @carlesmu in [commit](https://github.com/spacecowboy/feeder/commit/be63937d0a66e90fb78efd71da54b2050fe29778)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/c45efb8809b7138c29a0eef53840b8fc053d6ff1)
- Updated Galician translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/a3211c778c536bdf90c73f72030c2f394f7e93b6)
- Updated Portuguese (Brazil) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/da1703167971e1314f49bcd9195aa95540865023)

### ❤️  New Contributors
* @felix-otto made their first contribution
* @carlesmu made their first contribution

## [2.4.7] - 2023-02-04

### 🐛 Bug Fixes & Minor Changes
- Tweaked release script by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/01cce9a0017ca0ec12b53f552a47fa58ea3142ed)
- Added stricter ContentType restrictions on responses by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5fdb22d9bcdc7b99f1fcab191bf1802e0923b39e)
- Changed so full text articles are are not retried automatically by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2a3e3df167153be66c4b3ab1ea972774ea852dd2)
- Moved all article data to cacheDir instead of some in filesDir by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2caa0032ff7cd241735d7138f135e1018546b4ee)
- Fixed some issues related to block list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b69b4a0e99973e47acc59f0386344b66ed530af7)
- Fixed english translation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c4aa6f977b2b61dad2cb2c2eb4b7ebf0447a6b89)
- Fixed bug where backstack would get stacked with multiple feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/84d781dbaf0344e1b32157d2cb3ddd6c98c39194)
- Fixed feed navigation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8de0b6f22629be50e2cfba22d2d4929659e83f40)
- Really fixed feed navigation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c24a2c7725c3ef04a170052ad733ba7adb867c36)
- Refactored to follow guidelines. Top to bottom reached empty by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d0cfbb42b9e918f53196ed1f0c4105e235494176)
- Fixed navigation properly by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ee3c027dd6c9801be28bf76fc17f507ba78bf13e)

### 🌐 Translations
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/d3e912905d878369b7614d9a786ea69875ec3a16)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/5c04dc461ba5ffe612c688ba4301e8b8138fa068)
- Updated Greek translation using Weblate by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/5b1b6ad4fd8be46b06c06525dfd5be926f85b8e1)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0bbf5ef0df42faea95cd3ca8caf0b6e03a2a34e3)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8e4583c0e6a853567b59356ea3a9f09f54f93910)
- Updated Galician translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/c16eac5a4a91363515eddf1c44998785c1a86a95)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/163f27aea58783c53caac190767b130428f690b7)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/f0865ca1a67631814d6ff26293905a7f4b9c5396)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/173a35cad8638b8aa32480ede3fac902a914ba4b)
- Updated Greek translation using Weblate by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/598cc6a3b7602aab8b12e1c6f7e19497ee0ac84e)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0881c3d038a1819cf7ff1b45a3adc3c8b7c5891e)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/510a76aa76bf821bfb766c6c601a9894e286d551)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fbd47e773b6b192bf99d5bbdd0f7dafb49722077)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/c73a4c2ab8fa638e1cfd82c217dcaf82d0035c37)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/db0fa1b258803cfcce9c9256f2c79b62b887346a)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/50a1550c1cbd66b6c8cec9f1f76dd368427f7f4d)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3fe875ef01bfd89da7a23c808b81d7ea0f8b91fd)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/ecdfb3f31cddc28392718209855ca28a6318ec46)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4c60bc5ad58e43bfe653c7182f5e36ba1b48c352)


## [2.4.6-2] - 2023-01-25

### 🐛 Bug Fixes & Minor Changes
- Removed unsupported language eu by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8145d138cb3b00eef96a1f5e6322f240bdc0fe5a)


## [2.4.6-1] - 2023-01-25

### 🐛 Bug Fixes & Minor Changes
- Fixed unit test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9ddc1a14514c562e25271308caeb58b33c0ee8d2)


## [2.4.6] - 2023-01-24

### 🐛 Bug Fixes & Minor Changes
- Try to ignore if conscrypt insertion fails by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a87da794c6f38948493575b8d05f5079dd9fee04)
- Fixed typo by @slothtown in [commit](https://github.com/spacecowboy/feeder/commit/5e00030686616f6dea6a4f57f2283eefbe3ce9f6)
- Updated UserAgent to avoid some issues with anti-spam by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8adb4114c71ed801a517301c86c3eee98f370e54)

### 🌐 Translations
- Updated Catalan translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/dabcdbd2792dedf19ad0eaa0659533597d821487)
- Translated using Weblate (Greek) by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/e176df4dadadf984e8872974416d040790fe76bd)
- Translated using Weblate (Basque) in [commit](https://github.com/spacecowboy/feeder/commit/6998962757bc16a3831bf91547906db2d4b6df97)
- Updated Basque translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/af3ff07bc89d50f8902aec1d1aafef174da61245)
- Translated using Weblate (German) by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/2b1382df126f03d4afcace19e7a39639c779a07b)
- Updated Basque translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/21629a41bb48b924c0598b83a71f74ed079c983b)
- Translated using Weblate (Ukrainian) in [commit](https://github.com/spacecowboy/feeder/commit/05f2f0773e2b0f4cb98ffa5a1cbdd363b7497504)
- Translated using Weblate (Toki Pona) by @wackbyte in [commit](https://github.com/spacecowboy/feeder/commit/1e4572c790927bb4db7b76645de7ac0357aa5a00)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/b34335b13f492f24f125aa182feff7e3bde9ac79)

### ❤️  New Contributors
* @wackbyte made their first contribution
* @slothtown made their first contribution

## [2.4.5] - 2023-01-08

### 🐛 Bug Fixes & Minor Changes
- Fixed crash when sharing link to Feeder by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7a26e00d5cbf09ab4463605114cab9f967682d8c)
- Upgraded some versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1ce1f6b0703e0059c224f02864b17abce40f58a1)
- Removed Large Top App Bar because of crash when rotating device by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3ef1871ad99a1eb21e599c4a89e72fa6b6067fb1)

### 🌐 Translations
- Updated Portuguese (Portugal) translation using Weblate by @Cabeda in [commit](https://github.com/spacecowboy/feeder/commit/30455aba5ad1a7d7f846373b6ee532a1ff4436d3)
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/d9b58d61f5c7dfaad2f0b3393fa45e9b762a5b0d)

### ❤️  New Contributors
* @Cabeda made their first contribution

## [2.4.4] - 2023-01-04

### 🐛 Bug Fixes & Minor Changes
- Removed all static functions with DI in them by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/22996d2b3cd01179df834434483b787da4bd6126)
- Fixed crash on startup if "Sync upon app start" was enabled by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3dbb26b536b00b23f52969140f1f25b03414f75b)
- Consolidated all compose providers by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8109a298dd74a819badede0281752e0b360c4620)
- Fixed wrong colors for small top app bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/202dd81dafe4a4a1a5ef011a88e4d7103c793d0f)
- Fixed new item count not respecting block list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d45711eae77c8dab9a96681475425431d3b4fd45)

### 🌐 Translations
- Translated using Weblate (Greek) by @giwrgosmant in [commit](https://github.com/spacecowboy/feeder/commit/9518e7e81584af426b3e6946f13823c1e2b7558c)

### ❤️  New Contributors
* @giwrgosmant made their first contribution

## [2.4.3] - 2023-01-03

### 🐛 Bug Fixes & Minor Changes
- Fixed crash when sharing link to app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0657fd32800301cb837f10718460e185089cf981)
- Fixed a recursion bug with DI and some cleanup by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/27a040330efc302b573c009374bd20a4ddf9f0e6)
- Fixed crash on database upgrade by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ccf438c75899a23ba352ab512d4532c66bb82ab3)
- Show diff on release by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/855fe6d5bb2ee503fdbb02eb31fbd3e5df957117)

### 🌐 Translations
- Translated using Weblate (Dutch) by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/591c92cf0cf1ae04ec9aa47cc34f6fd620401720)
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b230216adb11671f62e6429504451a49273ce01d)
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/69e937796fe9ae99d1321bcca00527c473e3dd1e)


## [2.4.2] - 2022-12-28

### 🐛 Bug Fixes & Minor Changes
- Fixed spacing issue in settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eaf28eff36087e3c6cac211f8599c808a18f3311)
- Moved to GitHub actions instead of Gitlab CI by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/08da1d37f72562e47f7d5199ca5aa3fea5e0f3e1)
- Ignored some broken tests by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e62a390d9cb7498d0a0754b1bf79298cd83d37e5)
- Changed so block list now works immediately instead of after sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d172d6587486edfcd62199d520596fc320bef528)
- Changed so more devices will won't use large top app bars by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f224bca456c1ebe442e64876796685ba6619afd4)
- Tweaked size for large top app bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1256a87f8c6f033f676e221cff09717d922342a8)

### 🌐 Translations
- Added Arabic translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8bd20a6dbd7d2fbe227d98a8727f206a32c5024a)
- Translated using Weblate (Ukrainian) in [commit](https://github.com/spacecowboy/feeder/commit/d0dd8fa22fe695be2600669d212a1703bd70a5ab)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b435157732d0601b7563eb87defa3a878297eca5)
- Updated Galician translation using Weblate by @gallegonovato in [commit](https://github.com/spacecowboy/feeder/commit/a0f49a1a12c2754c3d0be69a3b6b8703ba481599)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/f4bb4abfca4f9baae78828fec5e636f7c28d1b5b)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/17d2f8486d24ab4e3bbdec04887dc647da95cebd)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/bf17b3e020486976cc99651177bb0195c5fb4201)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d1ea9ca3a69d41b00b1b3efcb482ac0a2047e22f)
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d7609fb276788c0bd99b42f6aa12b0265f0baed7)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/9c13bc8487c0658d0010dba877acdab0e2762756)
- Updated Italian translation using Weblate by @bagnacauda in [commit](https://github.com/spacecowboy/feeder/commit/b50bc9ad045857077ec6a843e91621d7d75e1ce2)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e9349b648010936d9bbe6356bf8be607b41d1092)

### ❤️  New Contributors
* @gallegonovato made their first contribution

## [2.4.1] - 2022-12-11

### 🐛 Bug Fixes & Minor Changes
- Formatted some version strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c23f3a05857237e66dab96d59e5d1f9b8d9feb55)
- Fixed some text not scaling according to settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2dd030b7ba2c79073977dce0b9f6e362d735767f)
- Fixed bug where swiping was not possible in list because of grid by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b83c5d381ab244d02251090375b4b0799e4d0d80)
- Added SwipeToDismiss to GridView by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/807722c72c5311ad5c9b64779d4b8563fcfcbda1)
- Fixed SwipeToDismiss so it works even with disabled animations by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5d0103a4519a64f925e6f10bdae5a6ca27edadba)

### 🌐 Translations
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/7fff2056b8872c2dfd8dd40f54c7a66fb683e4be)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/4aaaa2960e72616a5859fca9e50158827ff5878a)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/86b33eb1de7a2da22c7cdcfe8b1810ef009a61ea)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/872e93e212fc94ce83e428eb9b213a21c750cfe5)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/6e75150bacc9492f199af6fd9de392334b322929)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/6e2d9edd53b58d6efc86366b6265a319a6b49fa8)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4b2c87492a896fa2e3366b7c5bc3cf2e8207ee41)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/17a8a29c4a0b3dda2092201319ab6d9d60ae50a4)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/72c5bca53fd12ce2de680948f808b5030af78e00)
- Updated Albanian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/301cdff533245e252eeb7a0fd62fd0d6f29f1369)
- Updated Ukrainian translation using Weblate by @andmizyk in [commit](https://github.com/spacecowboy/feeder/commit/f5557397e333a9ee89479f15e2c2e0efdbaed9d4)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/640075c8cf121931f0ee17442769f8136f8e3a3b)


## [2.4.0] - 2022-12-03

### 🐛 Bug Fixes & Minor Changes
- Made WindowSize locally provided in Compose by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dd4bae11bdd546faeab546d06575175f74fe2cc7)
- Made TopAppBar larger on tall screens to make it easier for one-handed use by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e5ff3dabbc563674b2b2e6c675beb3492c32f682)
- Split Article to its own destination again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7f295fe773e1a2c2c7803762b1b82e34f327fb61)
- Fixed color of status bar and top app bar in Black theme by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/582c70881cc08f399cd59a61dbb7600644126f0e)
- Fixed PullToRefresh with new TopAppToolbars by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/279a67c0cbc03771931ef0be42c8702a573194b9)
- Added setting for Font Size by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f56169ee6009a29fa2ff77825cf4e7b08f4c5d48)
- Removed last trace of JCenter by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/582ab81f295960e6ca0ecab5872ed313c8b1a84a)
- Migrated gradle scripts to kts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3563d817cc50335f46ee5ff847e9b1c849b5d806)
- Deleted now faulty test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/335447fbf04a237813e4c26bad7b8fa5929f41bc)
- Added support for app specific locale by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/84648be44525a3b3cc452da5b2b887ba4bb627f8)
- Fixed missing navigation to feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9cf8e52ffba3b20c70e0b12764d966a4ffc81f68)
- Removed old TODOs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7362a70b4194c3b73fc9d3b165869c85d018a004)
- Changed animations from slide to fade by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/faa0e2344f05a8d3a66e3b642ac033c30390cb9f)
- Fixed mark as read being triggered on re-compose by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d1210dc0e6bd828716d0ccc7c6b6eec9a3184007)
- Updated screenshots by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5ad578c7f1e7455064c8653c989fb78d8889a945)
- Removed tests for missing screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7d13bdd1dab94618d778371109c05e0a57aaf965)
- Added additional excludes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d26beb648638b69facea7322cbe91becc197d161)
- Fixed fastfile for new gradle files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/84966849e9f8a936806a730c7ee771429b2eadb2)

### 🌐 Translations
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e91fb9231a5611746f041f9c5863f5014653379c)
- Updated Albanian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/fa0a128e45bce532990ecf9ff7cd0ae4c0d3da61)
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/b027d0c859bd8edaa7b047f30d44725a054c9336)
- Updated Vietnamese translation using Weblate by @tictactoe101 in [commit](https://github.com/spacecowboy/feeder/commit/0999d81e9756ccf6e84cff40af0fd591fd9815c7)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a39c41756713b1a16fe27116b05e21108ad89af7)

### ❤️  New Contributors
* @tictactoe101 made their first contribution

## [2.3.9] - 2022-11-19

### 🐛 Bug Fixes & Minor Changes
- Removed multidex library and use compose BOM by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/23b0dd4002bd0ad7a7d1ec3b941ded8d74ce1be3)
- Implemented StaggeredGrid for tablets by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ae04fa71eb4cdeade297e7b715dc3debdd7f9e7e)
- Improved reliability of device sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/866363b1a9594f4eae27544af419837cf133e468)
- Implemented predictive back by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8ead3242bbdd96103268a100bfd5ddeeb1c5417f)

### 🌐 Translations
- Updated Thai translation using Weblate by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/cfe4f5590a70d8ea993272d01671aae3825789f6)
- Updated Basque translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/c7c6c271f228e730724f1a0c85b9607811dc3d38)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/e52e814ae4aad3cbd022b8b92c1e415ce11ee30a)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4a56f2944f27a7f861b6a301617ef1e8754e03e8)
- Updated Portuguese (Portugal) translation using Weblate by @SantosSi in [commit](https://github.com/spacecowboy/feeder/commit/cfedc4d44a787756a1f0c943d4818ad6e37eac2b)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/bce0dae3a6ab5def13345a37245ea60fae1bea9b)


## [2.3.8] - 2022-11-11

### 🐛 Bug Fixes & Minor Changes
- Removed new-indicator from Compact and SuperCompact view styles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2f281017747acef9e192a5157a19800cc1eef765)
- Added app title for Thai by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5a43aaa007708c8ea65c0a0f069ca394d5663ffc)

### 🌐 Translations
- Updated Catalan translation using Weblate by @sf0nt in [commit](https://github.com/spacecowboy/feeder/commit/26b072525abd34750bcf116ebe059db488f073d1)
- Translated using Weblate (Italian) in [commit](https://github.com/spacecowboy/feeder/commit/d9af5801c30742399c65080688a27f4f29d0b791)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8a4304e4e1ac216a8eb14136fde2152377a52bb8)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9e04ef88bbfacdfb88dd4149e9616af8d7a50be6)
- Translated using Weblate (Italian) by @Fs00 in [commit](https://github.com/spacecowboy/feeder/commit/2106104e6625c7b6af2dc305e19104db5dad3479)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/45e30113a9b5ae4edf35caff00c95e2f1c5a5fd7)
- Translated using Weblate (Thai) by @bowornsin in [commit](https://github.com/spacecowboy/feeder/commit/8aa363b79ae739d1ddda71f4b63d9759fa01d0e0)

### ❤️  New Contributors
* @bowornsin made their first contribution
* @Fs00 made their first contribution
* @sf0nt made their first contribution

## [2.3.7] - 2022-10-27

### 🐛 Bug Fixes & Minor Changes
- Bold text on unread items by @camperboy1000 in [commit](https://github.com/spacecowboy/feeder/commit/f88ba58962738663e8d0790262440d844c974f4c)
- Don't bold the snippet by @camperboy1000 in [commit](https://github.com/spacecowboy/feeder/commit/dd6dbcdbe6f7c2061c09cdcdd81d2c4c307187e5)
- Add support for showing favicons in nav drawer by @meets7 in [commit](https://github.com/spacecowboy/feeder/commit/81e46e5bb7736eb4d9885c411d063a85ddbae020)
- Reduced title font weight to Bold from ExtraBold by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a0b960c176b69308c3918b6cd0ae272f77fcd3df)
- Removed unused parameter by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2020a70b2ac866606ffb04f53f6d8191cb174da7)
- Fixed text alignment in navdrawer after feed icons added by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/63b8cb53f0f1d0e0ffd958d743fbb833178d1cba)
- Added divider in navdrawer so text can be aligned even with icons by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3a93eb6b90a2926ca2db19548f8066ba475a128a)

### 🌐 Translations
- Updated Greek translation using Weblate by @zefraimidis in [commit](https://github.com/spacecowboy/feeder/commit/22fc67c0ca8f2a1882fccf398c5336cfd6cf0294)
- Translated using Weblate (Finnish) in [commit](https://github.com/spacecowboy/feeder/commit/267a1d836fbbbc62d0879ada5226f79bd33093dd)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/2863af80d0b93086de5de6a9066328242160ab78)
- Translated using Weblate (Vietnamese) by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/671d7af7451453a578c4546beb547a825677edc6)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/9328f4c8a3bceff6055f8a9dfb032114553c6f97)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/c29b1c087369e4f732c85f6e58c29dc71bbba22a)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/11bc8b68a3d5acdd4ff62220a27387a2d65f20df)
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/30fab5dd48353ea7557fa3c29455fbd5611c3367)
- Updated Norwegian Bokmål translation using Weblate by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/ee6fe99d83c1b26784c7a6c228aab5eb88cfd8e5)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/65a039ef55117742da60a5c6df1e30d6be64dcbe)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/e8df291a7c5e8c666762c18773d17995586c2c48)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d8882a290a76be94dbbfc4daa20af58faa367221)
- Updated Ukrainian translation using Weblate by @andmizyk in [commit](https://github.com/spacecowboy/feeder/commit/d457772fd0a0f006f79c1fceddc5853598d7f001)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/f057e3fb38de2bfa273e771e5577645ad36e196d)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/1dc28299914821959e482e7572dc7c9c24c862e0)

### ❤️  New Contributors
* @meets7 made their first contribution
* @camperboy1000 made their first contribution
* @zefraimidis made their first contribution

## [2.3.6] - 2022-10-09

### 🐛 Bug Fixes & Minor Changes
- Fixed parsing of srcset images in Politico's feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8e662118dc5844f09ce84e805bd26915627b9d5e)

### 🌐 Translations
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9b6b5a661c74baaa5e527a5800cd0c60854906a1)
- Translated using Weblate (Czech) by @miraficus in [commit](https://github.com/spacecowboy/feeder/commit/37a170df3a799c3800e170b976a91cffa5b1ab64)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/db9b825d257188253245e6a28de4b4f810530378)

### ❤️  New Contributors
* @miraficus made their first contribution

## [2.3.5] - 2022-10-02

### 🐛 Bug Fixes & Minor Changes
- Changed user-agent to match Chrome's by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ff23128bd637fd4af6f434d8914622a18597ed97)
- Fixed user-agent test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6002d91d2b748684e93eacea77616dc4e95dee5f)

### 🌐 Translations
- Updated Indonesian translation using Weblate by @liimee in [commit](https://github.com/spacecowboy/feeder/commit/e4090e152ac22cf8b1bf89bbfecc5a62b1fc898e)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/073ccb1d272567d6c165d2cc83c47b60374d5577)
- Translated using Weblate (Indonesian) by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/9c1acc5bec84329c775b905a3e5758c4647ed067)
- Translated using Weblate (French) in [commit](https://github.com/spacecowboy/feeder/commit/4cdc88bace2e52171cace0fe756facb6af7a5eb0)

### ❤️  New Contributors
* @liimee made their first contribution

## [2.3.4] - 2022-09-25

### 🐛 Bug Fixes & Minor Changes
- Only run validation if not a release commit by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a9b2179e2b09ec6db2d464db203f57a49742d198)
- Removed unnecessary DisposableEffect for marking as read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8ea3edfaa3c9e8a167ee26be1c0542c30957f9a8)
- Fixed parsing of additional types of thumbnails by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7036d422feb280ea5684d94d0418fd4f13d666a7)
- Further improved thumbnail parsing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a22aa5252cac741a35457d9ab9dac409c02d97b6)
- Fixed decoding where smileys would not get rendered correctly by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/943695c5cebc01ad383b013781d21149cc2c4b44)


## [2.3.3] - 2022-09-22

### 🐛 Bug Fixes & Minor Changes
- Added monochrome app icon by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/88391f35e91d06e0d17784645bfca05ef6a76313)
- Added a debug-only app icon by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/52a2e030d86a0ff4d4f02afa302fba9dd6c91bc4)
- Fixed crash when sync on when charging was true by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b003d5ecb5905915e51af556dc19eca880bb3210)

### 🌐 Translations
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/88b815b053929f3519f62ebf552d1f023e3dd869)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/6145fb2aea65f9cbddb68eb6cc927b5b162e4cdd)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4039f577bab1e0a3ab3ade4051628c4e3d112490)
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/819789ec0b69f490f7e41af4f880ef879155de79)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/1a9efa688f9365209305526c0c18bc7791ec94fd)
- Updated Italian translation using Weblate by @atilluF in [commit](https://github.com/spacecowboy/feeder/commit/e5b6191e8dd49b3798d8c8b01a1e5f43eec5299c)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/2d229786c5379a07182bafe67a57b6d1ebc25b7b)
- Updated Ukrainian translation using Weblate by @andmizyk in [commit](https://github.com/spacecowboy/feeder/commit/9d72357feda604c3a22791c20142d53ec29ac5ac)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/38f8e2fd5263416a861451706e52dcac4cbd42cf)


## [2.3.2] - 2022-09-18

### 🐛 Bug Fixes & Minor Changes
- Fixed padding in tag list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4ee8790ee3d608f59bee6942fc758d7d9954d3be)
- Fixed images rendering too large causing crashes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8cce8c0d2948386d8da6ce2520aa08ac7734b3a7)
- Added fallback to feed icon in compact views by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c660a68819a6419ef1c90e2d15dbb4aa3ff2ca33)
- Added blacklist for twitter icon as article icon by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2b91e13fba1e4fc950be561aca7897efa20a9d29)
- Added dividers in list for compact and superCompact styles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d4817632cc50b7d0389c3c4827b6b4992ac45fe7)
- Fixed so list stays at top when updating if already at top by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/605f9be2604ea361de269b5b024a13325553f73c)
- Added padding in list so FAB doesn't cover last article by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a7b032a16ba17147e334c9ab1c1d7fd02e624a73)
- Adjusted TopAppBar scroll behavior by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/54a1168fa96c6a9e65ac4a4b8bb30c68a3295d31)
- Fixed syncclient re-initializing unnecessarily by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3613b7580674524ae03f8e6db34fcc74f3ba81e1)
- Fixed reliability of read status sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a20f65e6efb4118b7eb2cee1548de3f02c43c2a7)
- Fixed image size in Compact item layouts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5a45015d38d22fc14bbc2aec54d4d2a8e8ca016d)
- Fixed HTML not getting stripped from alt texts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7ba2253bf76f6f269672b316b2d034a576023aed)
- Increased text size of block quotes in reader view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/738495a22961dc0e9b82e2193791c55d0c9d4eb6)
- Upgraded dependency versions and insets handling by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/925824dee29aba6f1c7c3f7cdc494fc7a390d493)

### 🌐 Translations
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/11505e3b033242ab0c5b29161eff72faf0fcc60b)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/863c832733347072c21f781c834a740e78f929f0)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/89cb8b85f35542d9b1770a856f27429e52d009c8)
- Updated Chinese (Simplified) translation using Weblate by @MkQtS in [commit](https://github.com/spacecowboy/feeder/commit/1d6ea3964b91168273f274c487b90d0b1270d3fa)
- Updated Ukrainian translation using Weblate by @andmizyk in [commit](https://github.com/spacecowboy/feeder/commit/e50de95c7cc33ec6d2f3818ec627ff62112689fb)
- Translated using Weblate (Galician) in [commit](https://github.com/spacecowboy/feeder/commit/257dfed6395c02a2dfa0c4d65123d1dd87c0cf74)
- Translated using Weblate (Danish) in [commit](https://github.com/spacecowboy/feeder/commit/6162c9e75d4272eea9a03d3da9127d3ee219fee5)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/383bd23cffdbecbf69cfc950cb0d25b954f07b61)
- Updated Norwegian Bokmål translation using Weblate by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/9c947b4cdc602b2555c734169720e9cdbada0434)

### ❤️  New Contributors
* @andmizyk made their first contribution

## [2.3.1] - 2022-09-09

### 🐛 Bug Fixes & Minor Changes
- Fix feed indicator localization in [commit](https://github.com/spacecowboy/feeder/commit/c07e95cea684dfa82849907679f50c989d8faba2)
- Fix Right to Left languages in headers by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/130c2319dfd542892d6ed691c1eaf5cd84a2189f)
- Changed back to single screen on tablets in portrait mode by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/35eb76607c0f773dd9b5e20f92a3f621e4921ef1)
- Fixed content alignment in search screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2ccf7d00a5a7894fab76ec9f07116f8277587e41)
- Added parsing support for additional thumbnails by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8a68f2a564cde2bbc4f01b5ba5ae4bd101704811)
- Fixed list not centered in landscape on phones by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fd46ece57a70eb08ab26007bb61f4c0bb86328da)

### 🌐 Translations
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/65b669ec0e789951e039d0215182b6fb52ed8b47)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/e37aba0cd14bbf77b9e2eda24a0a6adc3264a1e2)


## [2.3.0] - 2022-08-31

### 🚀 Features
- Added detect language to settings by @KevinSJ in [commit](https://github.com/spacecowboy/feeder/commit/65eb5d739ee44f0afcd600a89da1ad6d00a5ba13)
- Detect language of the article for readaloud by @KevinSJ in [commit](https://github.com/spacecowboy/feeder/commit/0444b16a5dfb049cfdad049165d3d58f0e3133e4)

### 🐛 Bug Fixes & Minor Changes
- Fixed all restartable but not skippable functions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bb2f8226dfb1f26341a007281323af8b2d63a3e5)
- Use separate description if language detect feature is not available by @KevinSJ in [commit](https://github.com/spacecowboy/feeder/commit/a4a3f8439d6917a19db3ee20a53c7c37e85bd2aa)
- Minor formatting change by @KevinSJ in [commit](https://github.com/spacecowboy/feeder/commit/45a63db494d30ef7a80120f33df7a258db7eb267)
- Fix typo by @KevinSJ in [commit](https://github.com/spacecowboy/feeder/commit/c047a0ddf6af0e45476e08fed509a40b74a2268f)
- Fixed some code formatting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/517efd703d6013ba31cc337587bf62796c2be555)
- Modified strings to be shorter by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d2828981af007b2b39eb72bfb0593d7ebd68a235)
- Changed detect language settings to default to true by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fca6176f0c6c0a3b0fa19474afc799054a1728c8)
- Created new read aloud settings group by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/942c3dd02354d60cbbee03a0b20691520f63a10f)
- Made detectLanguage slightly more robust for TTS failures by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b892765ecbbcd9bb66f1254c2579703a9d97cbbb)
- Updating test by @KevinSJ in [commit](https://github.com/spacecowboy/feeder/commit/b8b73ab19a3e3491239002111f2935d7675dad3f)
- Upgraded Android Gradle Plugin and such by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a442e159ef31425e62fc032d6f87a81c3b906672)
- Builds tools version doesn't need to be specified anymore by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c343a4b7a0edf51429f62fd1f9a367e6e38edf8f)
- Fixed deprecation warnings and api changes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/360b1d127dc2bb9aafb8bd4d2136d08d943dcc07)
- Removed accompanist insets - no longer needed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eff417717f5dab91848e19c0aa991afc16d3f60c)
- Implemented runtime permission for notifications by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e7c9af4f0359fad1c1038b1923b66509928ed4d2)
- Some test fixes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/25d649fe0f4fb6f158dbb8c9abbc623d22f89299)
- Fixed regression in edit feed view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0677cf1ced8e27253e29cf7c160664139021fc65)
- Removed all uses of live data since it seems buggy by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5ab974b7ce5a0818f9705defe0946cda3934d4e1)
- Theme by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/361617d5eadcc7561ac234795bab25bab6206cf5)
- Upgraded to Material3 - seems to work even by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ada5c09940639bebbed51398b387adf0449d6339)
- Implemented dynamic color scheme by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9860a501aca14ea21c4b5c07528399124756a207)
- Theme respects black again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a10b77e11e59e0d77af1215dde83948d55d49bc9)
- Fixed color of status bar and padding by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/aaee2b64861a7210d471a18809c8c6d9fa801571)
- More sensible size by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5799cc1aac6e15d8e4784eb9a2266a5a129e6c4a)
- Fixed nav bar coloring on all versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f07a65a35f5be414768a3ffc0bce4ee401df2e91)
- Handle navigation bar padding in horizontal by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7dbf7f26f1551fa2242ddc16d791473867d9271c)
- Try giving foldables more usable space by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8598f2cda5a65a35f506c7b342624f93909847f2)
- NavBar button becomes toggleable now on tablets by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a9392955250224118e20fd6bd078291036a076a7)
- Fixed more menu icons by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/497c387ced3d6809c6743b010861142d8a1ba2bc)
- More foldable use by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5a7c7850c5cbaf8ac18d7b14479bf8566bc2fa01)
- Forgot an activity by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7aba68929228febfe8511d6d608307271bb200f0)
- Pre split by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ef0958d1dacf16b7a3b7df5514dea5ede4abb2ca)
- Edit feed is dual now by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b3067123f74d823050d596796d1a361dc7689dd9)
- Search feed is dual now by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9f9dc5e1157b95e30ceafd845c640e9d65606b85)
- Fixed color of cards and spec says image should be rounded by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8c7e8deed07d250671fe8c61036775231338db10)
- Fixed link color to something more pleasing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1a0f6c930596ed0a6be2d05c8f56d24df8813f1d)
- Updated indicators on feed items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/89f7cdf2f92faa8b137f3cbfa9d770f12f554762)
- Removed unused overload and fixed FeedListScrollbehavior by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/92339e7dcd734325d6cb61b8256375ff2bada385)
- Some animated navigation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3ee4296f637f0a68a28b15c32b578767ade706df)
- Fixed animated transitions in sync screens by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3f87537808efbd219869ccdd30e3af8f2b1deff6)
- Fixed scroll behavior for sync screens by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/73b71df2b20b099c51555e8c4d71c325ee0561ed)
- Fixed a bug in animation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d1d4761bdcc9eb0a9cf1fe3ceca3ef805a030759)
- All screens animated and bottom bar fixed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/75fe24b0ee766858da5254d86689a9b52c2d5331)
- Fixed lint check by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/12c1468e08f4287fac782d0fe4346d47819a18ad)
- Fixed bottom bar padding for navigation bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5b87b130d07ab7ebb5ae2d19f4325b8aa94b8578)
- Fix text lines in app bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6000b1f64643cd2f547ace135bc1d1695ffc80fd)
- Fixed actions in top app bar to follow guidelines number by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/365e902c548a2326af675cddf1c973e9f501995a)
- Fixed incorrect default value in edit feed screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/feed23d45bcdcc13ab2b0d20701702e597e184b3)
- Removed buggy sync indicators in navigation drawer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9b4891680b3f254fbd7aea5d935b2b5408b1ca03)
- Updated fastlane metadata with new screenshots by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/489f3bdefaaf80c46756f819ddf005f09520fe84)
- Fixed color of indicator by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4ad565355c9fb7b2a592609087b385ffae10989b)
- Changed to canonical text to speech icon by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a6419890037865dccba04f3bf46b594a4ff8dd48)
- Cleaned up custom icons by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bbf0104bfb85598d3066d007da9ce4de706e1669)
- Changed to original done all icon for FAB by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2adfbac7d51df27267566d40190075b812abd0a0)
- Adjusted middle margin on tablets by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/af4a2dd13518f8d6c591947490e684153bbb3e0e)
- Updated to official URL annotation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/405d680b6860347608e50654fb1356a03dc2e3b6)
- Text given to TTS engine now has annotations by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a81a1b3cda0a97244476c8d2d026be3b3fa5e5b5)
- Fixed device list screen not showing devices by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/433a8bb18a2a78a2a72e37e33701bb3d45b45a4c)
- Added a skip next button for TTS by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/87a068a5449673202552703e97f3c7515a8617e2)
- TTS now detect language for each paragraph by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4b46139282d3bfce9937f31018c90548332ed2c4)
- Moved stop icon to start by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/25449bb326ac8e701e3cbbb27825fadfdfdd1d9d)
- Added ability to force Locale on TTS by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/85d7ddf3fa42b516719309970e0f8c314cfec85b)
- Respect user locale order if set by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2458d2f5b575af26be1b7e5baa5fc1dfe6c6209c)
- Revert "Updated to official URL annotation" by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2ecad20d7d0123bf564d3dc440ed26040813b789)
- Changed FAB to scaleIn animation as per guidelines by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/61486f131f86872410008725183a4a93d820f055)
- Added string for Text to speech by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fb431fa2ffe61eda105373c38cc99522afe1b0ba)
- Ignore RT tags in Ruby tags for now by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2e67ac5bfd292513dd4f275653b4b5b5145754a4)
- Improved handling of RTL text by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9a391a1b08b3bb48fb0824d8ae363c4d346dee7e)
- Improved RTL support in title bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2666f29403357ac84f54eaed7b56486c7afb09f8)
- Further RTL improvements - now don't strip ZWNJ chars by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8ae209c3e8d7f2c973563306c78093cc99945d32)
- Split pipeline so timeout is not hit by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5a9ab2352748a0a93a7061e353ed587c4927cbd7)
- Updated screenshots in README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e6b9aba61592088741efd67ef0118fb928e84f14)
- Fixed pipeline deps by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/af05221c7e380089371e8910476c071abc41f522)
- Moved Mark All As Read to top of the menu by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/38401846f70dc2958ad8a90298ee67365a6fd03b)

### 🌐 Translations
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/addd743a2a0a8aaead8c9b99ecab50e12904b7be)
- Translated using Weblate (Italian) by @atilluF in [commit](https://github.com/spacecowboy/feeder/commit/37e9840bb80ed33ca642db341a95adba0f34c512)
- Updated Czech translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/ace40142a5eea56dd1db4c30753b547f28f6f3db)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/2a89025bcfabd52b995c7ffe1b24b03f55920f3e)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/d9a7475d9ba35d201c8f267721346631c5e9b1e1)
- Updated Italian translation using Weblate by @atilluF in [commit](https://github.com/spacecowboy/feeder/commit/21f3b33ee7ff63512bf0e1fb517657a5e85eb3e9)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/0ec5096139b39daec08644606672beb7769b0a3f)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/fd60a78e2c3a796b2f5cbe7423935e6718fccff9)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/ca0895a48b7996754abce97593924b7284701112)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/464661bf3e9e731f8b83926e9a3cfea7f8e76f6c)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8d2291d26dd72a4237044c1cdb4aeba118a8a9f5)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/1105f0dac50167ebf9a4fc49c3a13b94131c81d2)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/991fce3e98fdd2e8bb9f6644f13082a58b6e4985)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/f6c5e8e7843542c8bc196605decf7aa887dfceed)
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/7dbc8c851a4533ae8fefcf8406f9e72e5fa5d0bf)
- Updated Italian translation using Weblate by @atilluF in [commit](https://github.com/spacecowboy/feeder/commit/a7d26e6a95305ac963a9b1089255d76f5300d187)
- Translated using Weblate (German) by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/165175156c6021ec1a8225e66dc53ff20a6b800c)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/87e61cc5f360930fbc6d374739bb716a2bfcaac4)
- Translated using Weblate (Polish) by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/8fc3bc2abf180e36a19ba91cf67fa2bef0184709)
- Translated using Weblate (Swedish) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1f0e2da9fcbaba32663c97e3abed9d5fef59f5ab)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/ecf364f4e94c6726ae9ae91566faf9a9c1ef6034)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d21a00759a6c5ecaaec77596f02b33bbfe7921bd)
- Updated Lithuanian translation using Weblate by @D221 in [commit](https://github.com/spacecowboy/feeder/commit/1377702363277706c423b5c02b52bb71bcd5a3f8)
- Translated using Weblate (Ukrainian) in [commit](https://github.com/spacecowboy/feeder/commit/c37a4b19b00b63787bd79578e2a04b124cdf4db3)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/7273a893d0af363dba30a3e029e6d3df91434aea)
- Translated using Weblate (Italian) by @atilluF in [commit](https://github.com/spacecowboy/feeder/commit/b43adaeff4b84a491f8f1ab22b1b57a0dc26f962)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4f4d9a4c82e03e1badc8cb663cc91741cbc9bcdf)
- Translated using Weblate (Romanian) by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/a664e174589b91f3c9f86219b60f9f9fa81d7b11)

### ❤️  New Contributors
* @atilluF made their first contribution
* @KevinSJ made their first contribution

## [2.2.7] - 2022-08-07

### 🐛 Bug Fixes & Minor Changes
- Fixed mark above/below as read with pinned items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c333c4531682351fda2f22e0145e1f8bcf021eab)

### 🌐 Translations
- Added Bulgarian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/317bdcd50f881a5c71e24a4d3fd6f6e8dbab2cb7)
- Updated Chinese (Simplified) translation using Weblate by @MkQtS in [commit](https://github.com/spacecowboy/feeder/commit/379c432905be582e737d0dac887e423ed252a6ba)
- Updated Swedish translation using Weblate by @bittin in [commit](https://github.com/spacecowboy/feeder/commit/27173e0257d42b80f704ab79a00fe5739c5266d9)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/60817d7b54d3af8c48b1bca5a01ccffa92f27d93)

### ❤️  New Contributors
* @MkQtS made their first contribution

## [2.2.6] - 2022-07-18

### 🌐 Translations
- Updated Hungarian translation using Weblate by @mdvhimself in [commit](https://github.com/spacecowboy/feeder/commit/b39803b667cdc4789d27a3e3986aff355d497b83)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/b9c1580266c43d0b24cebe2a1998f4b1f2406918)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/00e0ae0753f993adedc1072d0ac85fcf6295bb30)
- Updated Lithuanian translation using Weblate by @D221 in [commit](https://github.com/spacecowboy/feeder/commit/f6445c30c4ef49e98c8c50c40d20a600280959ba)
- Updated Spanish translation using Weblate by @FredMan95 in [commit](https://github.com/spacecowboy/feeder/commit/272e48000f404c4a739758a0b159534eab902c9e)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/62efdde34c139e228b7add41f47998bfc0a083e2)

### ❤️  New Contributors
* @FredMan95 made their first contribution
* @D221 made their first contribution

## [2.2.5] - 2022-07-07

### 🌐 Translations
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/2b5a0bc79785c8b12dd23a55da98159a09280d20)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/648e9d5d2dfd22d23ac41205d1de68db56a2479e)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/14c668743d667a4b7b3356adcece75759aa2b706)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9a3f100af30ab9e4a27121fccaa8643794c3ed2b)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/59c9f5d946b716e59b3f550b927562b25f95148e)
- Translated using Weblate (Russian) in [commit](https://github.com/spacecowboy/feeder/commit/7dbee822fde261e569258c8b49dd45e0d6cd4a44)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/c410021f080cbbe9f41631128ebea7a73103787c)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/c51289a7b457bf527b6a057293e14897ff3a4348)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/2161a18a48c343f03f640a8dabc41c1c8682985f)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/bf97189705168c9bb89f5d9b76e42ed54dc381b0)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/48862b193ca96f59e7c67b74df3f4d008e5ef7d4)
- Added Hungarian translation using Weblate by @mdvhimself in [commit](https://github.com/spacecowboy/feeder/commit/3b2a72d985f5524fe6c0085ddee3e71a3787cfbe)

### ❤️  New Contributors
* @mdvhimself made their first contribution

## [2.2.4-1] - 2022-06-26

### 🐛 Bug Fixes & Minor Changes
- Fixed permission group by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/624c3160876c6d690645b3638d79e9ddc4a7e9c2)
- Update .gitlab-ci.yml by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a771d1d25a38938b617100ddef9ccb2cc502816d)
- Update AndroidManifest.xml by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/979b45a9ceca6a1ac67f02daccec6fe4123b4ad9)
- Removed content provider temporarily to fix install by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/252bbf579da218745ae193a193d196e174afd0a2)
- Revert "Removed content provider temporarily to fix install" by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/97727bffa08def38ab1692fa7b72cf5c709509cf)
- Fixed content provider by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b8f4f64e8a48ea3747888b43bf4508ee294f6887)


## [2.2.4] - 2022-06-23

### 🐛 Bug Fixes & Minor Changes
- Implemented content provider so other apps can access data by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e236f0bc9a6c0a8095855deda26cfe7542a84992)
- Improved speed and reliability of swipe by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1ae8eb078091fbb35f2272179826c0a498b5cf25)

### 🌐 Translations
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/62f9c28fd655e0732f6f06af5d6a4079c7bde61f)
- Translated using Weblate (Ukrainian) by @defaultpage in [commit](https://github.com/spacecowboy/feeder/commit/3f1e94b00f5a11e6fc17c026b39dfe4bed732f19)

### ❤️  New Contributors
* @defaultpage made their first contribution

## [2.2.3] - 2022-06-18

### 🐛 Bug Fixes & Minor Changes
- Removed duplicate portugese language by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4889a5dcb27394f3875fa7a3a5707008a1c7cb5d)
- Removed duplicate portugese play store translation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6ca4df09766712c785f6e61289aaeae9f6b61638)

### 🌐 Translations
- Translated using Weblate (Galician) in [commit](https://github.com/spacecowboy/feeder/commit/4c03fe854ea978a5cbd9401d59c916157b2eaa1f)
- Added Portuguese translation using Weblate by @weblate in [commit](https://github.com/spacecowboy/feeder/commit/6c49bc96a0d50ab35f8b1e8b996a098e87923305)
- Updated Portuguese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8c81a0edcbddc5cbcd6813d7bf84bb8fc617a137)
- Updated Greek translation using Weblate by @Tha14 in [commit](https://github.com/spacecowboy/feeder/commit/d3ea05e163cc427f1254874d52c185cf4ffa600d)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/b7cce27857fedf9f0cf426c1a5f13cf46d0c7f17)

### ❤️  New Contributors
* @Tha14 made their first contribution

## [2.2.2] - 2022-06-11

### 🐛 Bug Fixes & Minor Changes
- Fixed list incorrectly scrolling up when marking as read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2bf95ec2493cf43f90a3cf68965b65bd7f2f98db)

### 🌐 Translations
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/1d9c7dcd127210d640575081f581b5b0053876fe)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/40ec83157a54c095ebb473922983e31c28699de5)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/5f9dfafda2a803a5ce76166138327e847516d8ec)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/7e74a6d95ad72093649cbfdf1f96749827beead3)


## [2.2.1] - 2022-06-06

### 🐛 Bug Fixes & Minor Changes
- Fixed crash when feeds have items with bad links by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ed0f3c330594497bb51cab090120f962a403cb29)

### 🌐 Translations
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/8355e4b16dfffecd07e5ce42d5631ed1cf7bf7cc)
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/41c33eceafcb46319b7ad9841f073a8917777563)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/76baeddeac92cd5c4db9ca199ba3e1d64b9cfd92)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/5b1c4f60417add52fdef3062eef2ba12215fa0e2)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/7247ce08d0ca2a08c5bb1976c96a0a5512acdd1e)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/88ebe3cd42de87ca539caf82810894b600c26d4a)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/7980253d9eeb59f9d6a70b4c11966e3410d25a8f)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/576be0f3fa5a4d1c0373f21e2bd6b69f79e6f5be)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/f99360d4b320ab73ae4bb7f1177137ad382649a5)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/575fc3b392422c486eb8e4bd6920ebe719380504)


## [2.2.0] - 2022-05-30

### 🐛 Bug Fixes & Minor Changes
- Update schema to add bookmarked status in [commit](https://github.com/spacecowboy/feeder/commit/6f4bcab28fe5dfd8c25f356443559d95ed05a32c)
- Toggle bookmarks in [commit](https://github.com/spacecowboy/feeder/commit/0ab5b1bbd092fe0418bc9949a311d62f68f1d66b)
- Allow to filter lists per bookmarks in [commit](https://github.com/spacecowboy/feeder/commit/a90b04ce2fbba519d8e6859d4d2c360b5f71cefa)
- Align bookmark icon to the right in [commit](https://github.com/spacecowboy/feeder/commit/2ad907efbcd4a758198c4270633b9febf48520c9)
- Fixed DB test 21->22 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b8b414a63bbafc74f21c3c081c4954ccf05f6ceb)
- Added DB migration test 22->23 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6e79da27a65b864c4fb84e61fac0ea6998aa4b54)
- Remove unnecessary code for bookmark's alignement in [commit](https://github.com/spacecowboy/feeder/commit/a610cfaba4cb682b73ef1cb72e1634f105d1a21e)
- Handle bookmarks sorting by date, feed and tag in [commit](https://github.com/spacecowboy/feeder/commit/132ced2a6bf1a3f75fc55d115a44dc0d934fa197)
- Removed Galician language when deploying to play store; not supported by Google by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3ffe84f150ab1ed0cf16249365c32db4afcf5fc1)
- Removed Galician from App; not supported by Android by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fba8c0dbf910fab84fcc152c88630d902dde99c8)

### 🌐 Translations
- Translated using Weblate (Galician) by @antonpaidoslalin in [commit](https://github.com/spacecowboy/feeder/commit/356d1c26c1ad58638d6ef966f86838d14235afce)
- Updated Italian translation using Weblate by @bagnacauda in [commit](https://github.com/spacecowboy/feeder/commit/35eac7c8ec307132cec15009fa1b6d0522cd94ad)

### ❤️  New Contributors
* @bagnacauda made their first contribution
* @antonpaidoslalin made their first contribution

## [2.1.8] - 2022-05-16

### 🐛 Bug Fixes & Minor Changes
- Toki Pona is not supported by Play Store by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bc4c314c550cbe1b66378be38cfcbcfd4e2e7e90)
- Toki Pona not supported by Android by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5b46fa0aec3b6f89f06f76f3a30912df69d3d206)

### 🌐 Translations
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/bef7d13c358c4da4d522ece6a870b67cd3140c36)
- Translated using Weblate (Danish) in [commit](https://github.com/spacecowboy/feeder/commit/72d5b3fb7547995f21dbc6a2ba16b4b3ec2f675d)
- Added Toki Pona translation using Weblate by @ducksays in [commit](https://github.com/spacecowboy/feeder/commit/e0d24b9180e2399c7f05b3bbd90acdc189096e7e)
- Translated using Weblate (Toki Pona) by @ducksays in [commit](https://github.com/spacecowboy/feeder/commit/5be9e915889c7a9cb4add26348e27b39fbd345ca)

### ❤️  New Contributors
* @ducksays made their first contribution

## [2.1.7] - 2022-04-25

### 🌐 Translations
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/3ff53b659ee33101874003be93137ab67d93f27e)
- Updated Galician translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/09af6ab5b7a5521dc2b4dcb403156f98dfb6588b)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/10e8db6056845be60500ddf33ccca26c33c3f3fc)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/40bb39a833853c0a99cec8cfdcadc1d5ca74d16b)
- Updated Portuguese (Brazil) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/93606b38c0cc51ff2374754b47f3b96e0c2e2a7c)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/26e075aadc6d9869d2ff73156620d415dc2b6901)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/aab536a08ea48294ee6a04a752a819b469065fe9)


## [2.1.6] - 2022-04-10

### 🐛 Bug Fixes & Minor Changes
- Create Block List that filters out feed items with a blocked word by @ManApart in [commit](https://github.com/spacecowboy/feeder/commit/e02882973538c7bc7dcfdfb5e3132c02e55dab96)

### 🌐 Translations
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/17dfc13011087ffd9633917d9da1dddce3cfa81c)
- Updated Serbian translation using Weblate by @eevan78 in [commit](https://github.com/spacecowboy/feeder/commit/760e6da0b892ccb98369cd98e17d634c73b4fc1f)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/ce5d1a0d310cf8c3a8408f2a1fb8c22c44f024b7)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/ca5e5fce26eea40728f10b660ab4328d0a56f79c)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/53c9a700f140cb7703bed8ebfda9ccb752c6bb45)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/bcaeb6009f832d9fa7cb123af91ade742dfc9ae0)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/0864c9f5224c89718a26b2fe68dda53d73eb29c5)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/47fc2a0a52cfc3860ced8480fac222882015d8d7)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/377e36c47630968d5c5188a6a54e224b671d9e38)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b2a24f8a4374a077a687bf00e10e62ed3d9872a7)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/9376ad3cbdb3da142cf97fbb131d2411ae28e864)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a045d6bd9b558aab1f2566212019456b9388e043)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/afa7710710f69defdb75f313ddc93b6369e55268)

### ❤️  New Contributors
* @ManApart made their first contribution
* @eevan78 made their first contribution

## [2.1.5] - 2022-04-04

### 🐛 Bug Fixes & Minor Changes
- Disabled Sync API request when not configured by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0692737a8afb87d380a059749df4d26b15ff0509)
- Batch read marks to lessen API calls by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/48bf588ffa578fb87046287c0b573eb888ae4fe5)
- Fixed crash when removing already removed device by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/559c5e504a6247a0c46583668d77da74d8ae56b6)
- Also mark as read during regular sync job by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4941a97e584dbe3d85ba7c009ee09e51685f841b)
- Moved all syncing of read status to regular sync job by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/787917585c4ed4a611f8e60a3794169e023aeeca)
- Also hide this behind a guard by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2f2806018ea97c8cbbf9191a059f0c2ffab18a17)
- Update remote to new remote URL by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/87c1f9259c8155d24bb25aa0559b6b8f69e6d1a8)
- Logging improvements by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cecd90420f91ed05398db742b3c7281666a6b249)

### 🌐 Translations
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/abb2ed1cc8d32227469c4c191fdc73acdd2403ae)
- Updated Turkish translation using Weblate by @oersen in [commit](https://github.com/spacecowboy/feeder/commit/8f5c9838e943930192310ff4e462c9366264abd1)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a3a24eb268642c3e076f195f553bd137f44a332e)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/106499ed251e98892568054637f494a803bfec6a)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a87e1b91f791c11f2aed8e52f5a2c15a99367240)
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/1f14b0050fc52b20f328cbae3edd0400e4656bf8)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/9951847391c40121e3529f492daafb1a249cd4d9)

### ❤️  New Contributors
* @oersen made their first contribution

## [2.1.4] - 2022-03-29

### 🐛 Bug Fixes & Minor Changes
- Ktlint format by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ce2f7648cc9f0e9501c7469bd5f7f311faa5d68d)
- Fixed a crash introduced in 2.1.3 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b218326ea82f5a97624cdf9a39b760e0375b232d)

### 🌐 Translations
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/066ab7136836d23e53bf82a9c43e583b15f08663)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/369136d5cb5b9b42ab8498fffe8ab642d79000f3)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a7c0746be2e96e3feaedc07ec9fc454507e7104a)


## [2.1.3] - 2022-03-29

### 🐛 Bug Fixes & Minor Changes
- Added ability to pin an article to top of the feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4bc528b72146e49c68ff1ff1d2935b6aea46754f)

### 🌐 Translations
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/9a91d313ddd4afb42196f2c6652a6ee3bddef102)
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/474462cb867f08726f0ab2fb005d20d8ad696949)
- Updated Chinese (Traditional) translation using Weblate by @walkingice in [commit](https://github.com/spacecowboy/feeder/commit/d69a0490657d9b651f31f015821418635987c9ad)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/059041bc63faa0ef4e9d1c5fa9579c5e135cac4c)


## [2.1.2] - 2022-03-14

### 🐛 Bug Fixes & Minor Changes
- Fixed broken test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d06407a572c53590d7d0cf327e0bf5edda54afed)

### 🌐 Translations
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/c7a0936509d899600d0dad41b5199a7052c96224)


## [2.1.1] - 2022-03-14

### 🌐 Translations
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/69437712921e4e4f08ea55f5b78343f34f0bdafa)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/9a14699f1c93433c8da2dedcf0e7d528e5b4c8e9)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/99636a463495ce612b6aa59bb6cc3f73e246d907)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/f2c70991dd5a23d0885fc113bb152975aa331aa3)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/6fe26dc0340b80340354b5a7d8c495f6b3f7298d)
- Updated Chinese (Traditional) translation using Weblate by @walkingice in [commit](https://github.com/spacecowboy/feeder/commit/95ea703a8747f561a6de54064b7877044623b818)

### ❤️  New Contributors
* @walkingice made their first contribution

## [2.1.0] - 2022-02-22

### 🐛 Bug Fixes & Minor Changes
- Fixed incorrect bundling of notifications and sync notification by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4cfd9e2eefb61e693aa1411b19c13a0f76a04083)
- Added setting for swiping to mark as read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/96cd9754b5ca9480378a4afd5609285ef6edba7e)
- Fixed summary notification not getting cleared by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/154ad3563e240a40e53cb96890f356a258d6a531)
- Changed all header sizes inside articles to be the same size by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/416bb58063fd45f87c6fe611f1d19c16621f7ddd)
- Fixed OPML export file lacking .opml suffix by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/16ef3dc569bccdd2300469011c466de717fb67d8)
- Fixed Play store locale code for Danish by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/211bcf43dc92ee2b257cc0a81fd57f0bea065b70)
- Added confirmation dialog for leaving sync chain by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1f6a35cc35e21f6d219290dae3b1ae28afe73fbb)
- Added message if barcode scanner could not be opened by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b084f45459c928783e8d587c601d2b57f7495196)
- Fixed navigation bar obscuring UI in landscape by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6364a49b28aa1042aa19651d7a64b6ddc29b7675)
- Improved read aloud by splitting text on more punctuation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/be6f312da86a0af6b65f98bd91da57c0ac2f1e04)
- Fixed crash in case Play button in Read Aloud was double clicked by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f2f5fcfe6e4219c81714dc46ca9522c710cc268c)
- Updated sync code to match server side updates by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cbe64e8b6a751e914d33b7f9b21cb7bd7d0ac688)

### 🌐 Translations
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/dcf608daaa90aa7679fcb3bc3c360c7b8a8440f6)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9cfd2ffcce87e12cb356edd6b95cfb06147f5bab)
- Updated Czech translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/dc307594c6f34daacea8ae2be2a15e92ed184de4)
- Translated using Weblate (Danish) in [commit](https://github.com/spacecowboy/feeder/commit/8666db6924f60b774dc47b978311e1f84406b5a7)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/7a8e945fb00d05f804b3fcd744da8efc3c016c88)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/bc1762eb08febe48ee2f06b878f484963cc93268)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3f59048b659b9ffd7bd8c90e742f98554237becc)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/3d0f713ad1675f8189d5ccff1c96c6d95c6c643c)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/c1ffb04da0005be05c48611c587c1d00479a1e13)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/dea4cf5a81b5dfdd20df0995b633734d3d89d233)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/558acdb89130aea9ed72d005aab97a1e98eb22de)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/9b00f1d3d3ab03f9d2b6da4eca36c60e586034b5)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e59a8590d8c712a6446caaf57d6e271e83509683)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/fa7a86a9b6e770a80536bab912b65218840de3cb)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/231293f4c57d7a23feb2242a449668f21732401b)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/2f615a1977780b02981e938e191b9afc1dc5f35e)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/a8e30f7fc4dd945cde2eb15974ac5cd93f09121b)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/f0fb62afd5465bce6ec8c1b2b65285ebffa895bc)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c7e012698b3116cbc1ceb33c33559ca3e80d4422)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/84a4796382c4a2783bbc35313b19efe26805a911)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a5277f5076b14a9bdd36dfb83c9c14c71f3c07a4)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/292e58faace3d79bec6514203f7766d0eebd078a)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/810119193403905cd28a73011d236bbcd7aaa549)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/6541d7c18cb93ec44c2321dd8e5577355552ee7f)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/c3a001806c00b91d2ad93f294fccaf77b2ab3b1e)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/49afe9e71b9ed8dc4c01d3cf7372eb838156b81f)
- Updated Danish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/06039102cb0e80877c550dc9053e89fcd00a851c)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/496f8abcf8f1876a538b8b0de731bfaa9c605051)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/b26c61ee3e42ce508f9e51293958723e9a41f4de)
- Updated Greek translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/004c644476305137e91846b349e8835597c77601)
- Updated French translation using Weblate by @VeryTastyTomato in [commit](https://github.com/spacecowboy/feeder/commit/5959c51296d08b7b5f26bc44e6afd62565368146)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/5aca1d6e725755361e684dacd800e14de76e1bd6)


## [2.0.14] - 2022-01-29

### 🐛 Bug Fixes & Minor Changes
- Implemented multi device sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/24c0c8fdec35073fe06ca7fb1da836b4608594e3)
- Fixed spaces getting replaced by + in feed titles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/211b1281b735a61d84898c8cf04d6c66bc86d95b)

### 🌐 Translations
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9957d68b66931ea4b5a610ec97a8dc6f68ef4531)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/5363e6c922b8b1ecdd43014cfae1c8306f4822f4)
- Translated using Weblate (Czech) in [commit](https://github.com/spacecowboy/feeder/commit/33b4f94264a373e0381f499b3621112d52e70763)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/cf2a62e602aa00c5b4413b68cb7720eb2176ddbc)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/8004a566b7c1854c5c8a0f0d40a2ffc693213eb7)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/c68321b86485aface794c2aaa497b6044e2115a0)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b74364d3ed64c13f871c6b046e25808af6d35459)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e7122d86c00faba511ed08094b7961f581f43b88)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/c32113b2e21299157bb9a5811a8a6cc81a033ecd)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/312f1244b61e23a51d9ed90b7ba0074e86637b04)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/c5bee2a475b05d2e5c04783b26351daca76f9397)


## [2.0.13] - 2022-01-20

### 🐛 Bug Fixes & Minor Changes
- Fixed open notification not marking it as read or notified by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6ee7f869464acd2b69e689241fd89976ade5ced1)
- Fixed images using srcset but no src not showing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/683da3e0af8d036d28a9b04c6e240823029cb65e)
- Fixed locale name of Czech by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ddd5cedf8e03e15c228e6064258d65fcfd687158)
- Fixed Czech locale name in Fastlane by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6c04a9ca232dec342556dcca74dc8dac0a0f2ccb)

### 🌐 Translations
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/1163042056a56c6e76f7fda4999e89640b58256c)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/9c1befbdc974e1c7a833fac366e481e7c9497c70)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3b4d66fa490dc28638dddd99127a75c71083ee5c)
- Updated French translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/896b575fe25267d6200fafb39fdb0c1b010a3e9e)
- Updated Swedish translation using Weblate by @bittin in [commit](https://github.com/spacecowboy/feeder/commit/7dd75f18da5828728cb43fc518d85b16f86882da)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/60c71749dd9af249579d4d48d83d79c6277c7e36)
- Translated using Weblate (Czech) in [commit](https://github.com/spacecowboy/feeder/commit/9e4460b1628226b290bef27d8f83dafc73147563)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/a69440bda4bea521d829441ad75a947170aff553)


## [2.0.12] - 2022-01-04

### 🐛 Bug Fixes & Minor Changes
- Changed sync notification icon to a different icon than regular notifications by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/54d33a4f70c4b23908696297245bca9cba68886c)
- Fixed clicking on notifications not opening article by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/72a3d842a9ba9bd8d975f076449f2c0f673aa2ae)
- Fixed feed responses being mangled sometimes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/504d0339ecd07651536a265773c0a04f33f7bf5f)

### 🌐 Translations
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/f768b9ebdc3d680a89a842a6926ebcde03126ee8)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/00a9817305bdd5a7d3327d5c72162898cb46ebc5)
- Updated Indonesian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a08922ba86912aebfa9dc18c1bc122fc407c9eae)
- Updated Norwegian Bokmål translation using Weblate by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/ce11cb59ad5b4e127da080b32b8ff002808de320)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/7daeddf9c022ce7e873d3cd9c4b8504fc16a7259)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/38dcf8069d5f686b11907ceb4a24840069e75b0d)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3cba889dbea23469139b220f7f72adf9d48c0ce5)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/7d6eb34703138236470202fe363b6945e3b204c5)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/bbe1a9c0f1d202eaa7fa90c28d82303469a44719)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/750a06653f39d73f5b142025781f695448111234)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/71228fb782c37b2aff00cb55fd700dc26a6c1c19)


## [2.0.11] - 2021-12-30

### 🐛 Bug Fixes & Minor Changes
- Fixed crash when opening app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1e4ecf09aaa396dca5ec241ed5ea28ef1d7b8bf4)
- Fixed a reported crash (rare edge case) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/64b0c70515fa60a0a824cb535fb158bd692003cd)
- Fixed rare crash in case no TextToSpeech engine was installed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/735190fdfa8c76dcbb0f5a702897f1bb486b5503)
- Fixed UI getting stuck in a weird empty state by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cedbb7ea26d563797d392dc1455dc792d5ec0932)
- Added some handling in case an open article is deleted by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8a11ce871d4f4b2646e5612090db02440d38b981)

### 🌐 Translations
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/0d968db5e465b3e3143d307ee1e0fd57ec652aa1)
- Updated Portuguese (Brazil) translation using Weblate by @gutierri in [commit](https://github.com/spacecowboy/feeder/commit/9ec9cf6a4384e20591630b905236cbe222794fed)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/494e7a5625a709d12609c4ed3259ab0b69dc4279)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/2f4ed127e29f2dd789e8ac14b8737f05f87b8e92)


## [2.0.10] - 2021-12-20

### 🐛 Bug Fixes & Minor Changes
- Fixed open in browser opening wrong link by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6bbabe68ce31ac624a61faaa978cc1c6c421fb6b)
- Renamed folder to match Play store restrictions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/696203ead96ffc9fb766727d3e3f9c086cf0688d)

### 🌐 Translations
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/b10efbc58883ef998203c1020fbbae8aeef969dd)
- Translated using Weblate (Dutch) by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/1ac2b3317755e038bad6e09ca869bf5ffbc6281c)


## [2.0.9] - 2021-12-18

### 🐛 Bug Fixes & Minor Changes
- Fixed app not respecting what to open articles with by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5c9259cccd1a81cd59e2815dbf4d15227bdfc2aa)
- Fixed notifications not dismissing when reading articles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/28618bc94e8199a5058d57a5f9743b908525c837)

### 🌐 Translations
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/1600cb79c9f1b8a34afad2e3eb0648ba3743fd66)
- Updated Dutch translation using Weblate by @mm4c in [commit](https://github.com/spacecowboy/feeder/commit/b85fd95ce22d7ae97161ebc55df1aa6ba1c1a6fa)

### ❤️  New Contributors
* @mm4c made their first contribution

## [2.0.8] - 2021-12-09

### 🐛 Bug Fixes & Minor Changes
- Fixed hardware keyboard support: ENTER now works as expected by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c4228d4702e08cac2c5085f4de19e948b36e0730)
- Fixed sync indicator getting stuck sometimes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f976af7547a8e3cea2fd8f360961b256d73f94fd)
- Added sync progress indicators on individual feeds in nav drawer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3d3da496b927e44ec9bc81291b1ae0766fe42f94)
- Reduced minimum feed age for sync to 5 minutes instead of 15 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bb50518387c5c9ffd6893146917e4b854ed2c452)
- Improved error message when OPML import/export fails by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/50aa5e47ed7b20229e34b901c1698188186fc4e3)
- Upgraded and added some dependencies by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/df92ffab2e3ad19869b1dc2dc167d1a273770df6)
- Implemented Tablet only interface by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c993b876bbfa7b0c18f6c09cf36811aac3c0713d)

### 🌐 Translations
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/55006182049b1b6f01feac979fa4d562ac6a4de6)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/3bbb9216e9bb77f347d68a8b7d835e6780262247)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/993a07b87a7790eb4245b400a350595ba8626f39)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/b88573121254cb77d20258ee83df34a378d6ac77)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/23354741493139186b642a5de38f7192b8dac04d)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e3a59bbed8638b9f03a695ad5f34a48286592942)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/faf4d7d37faaf360e4ac310fa4c87626aabbcfaa)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/7bbed7f18557f053477397f62aba2809f344e468)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/818745b87da03b5e68e2388da37d2ced93924070)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/200be0ce9aff279eaf5ad8289cf4a5063adc864f)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/1793a4a436ce18aeedfcc802dc04a6378b1f9217)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/0e4856ed74f52cae4f3e15cdab87bfc3efd4173f)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/af0b95e60be0bc1edc664d58df483f8c0ebe7947)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/60cc04e3b460eefe724b3099b9602d383541127c)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3b7163327e682a206de6c169d47c7d168037c71f)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3cdd9b1cfa8de8b41688f3b502f64a14de59ee11)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/d020154718959d665ef847a379474f9c4c8ee490)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/046d8cddd63acf0ab5702e2cca5fa74ee1230462)
- Updated Tamil translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/2dfed712a77e3890be1381e00e958e6395f28ee1)


## [2.0.7] - 2021-11-19

### 🐛 Bug Fixes & Minor Changes
- Fixed crash introduced in 2.0.5 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1001815d7d58b0a8127b1747f77c9ac7498eb64a)

### 🌐 Translations
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/87cf87f4566eb18ad11b305203f28d49ead8ec75)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/4b67ae83f7019e90344b82bfb5399622d456bb83)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/17ae3a979c8add504222d4ceba87d6d09f5c1faa)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/eccc086f5ed8d510933b91e062203dc77e859e38)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/450919e51062da69f54bcf931e45526dbeb90e91)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0449b989eb780318e60cbb44fe13ab4a921ec7c6)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/0e94edb63b7462cdbb814c169369a3037d88eadc)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/5c30def869416712714a93c23e39269fda4f4416)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/9eb6273d83d406f7955ad3ffd896c052a5e7b4f3)
- Updated Portuguese (Brazil) translation using Weblate by @vitorhcl in [commit](https://github.com/spacecowboy/feeder/commit/a00177be036f909e9af3b856b2d7a2a0441a1edf)

### ❤️  New Contributors
* @vitorhcl made their first contribution

## [2.0.6] - 2021-11-13

### 🐛 Bug Fixes & Minor Changes
- Fixed scroll in Feed being cleared when going back from Reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/604164437cf0ec1405dbfebd397a772fd4591ea1)
- Fixed translucent navigation bar on Android 23-26 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bf7de231d991edf909d0a6bfe81a8b486f9add2b)
- Added reconfiguration of sync when changing sync settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/de68372659d01febe1ae4ba61a9ac159ded49a91)
- Stopped requiring high battery for sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5af7f79bc8e09f021d5dc5eeec1cce495cdbf344)
- Added code to ensure sync is configured on app start by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e7a2f36476ae94adbed5de998043fb5b9c6482d1)
- Improved support for background restrictions in Android 12 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/be5d7540ed16485c4a8e7ab1cf4b888b89f60e1b)
- Set periodic syncs as expedited too - see if that makes a difference by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9ce8936548243365c3acf31c4a6b004b23dc8940)
- See if batching FullTextJobs makes a difference by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9438a368d9b939435f37dd2cf461980ba4361f61)
- Improved reliability of notifications by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4193fd742d269f603f1a79da692b5d65576a784f)
- Fixed a possible collision with article IDs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5014172b94b865554afebbb3042cb64e54cde82c)

### 🌐 Translations
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/5b073871faaaa307a43db0a5c70414f340366fac)
- Translated using Weblate (Indonesian) in [commit](https://github.com/spacecowboy/feeder/commit/f4801944ffb4ed33b35ac7e1543c02db644fc7cf)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0881dc4a1b4e6e3ff5a30e86d55f3af90eb513ff)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/289553fa5afa295d7cd24802ab21629f3c8832d8)


## [2.0.5] - 2021-10-31

### 🐛 Bug Fixes & Minor Changes
- Added missing string resource by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e83489e2eca123b11c63c7fa9c163cb495d143e9)

### 🌐 Translations
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/12554068f804fbeea3177d2ebf2de559601b4f37)
- Updated Norwegian Bokmål translation using Weblate by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/9ce82f0eb82a522476cab287c6f092cbdaa9b3f3)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/5f7ae0c735391884abf4359dd7a7d1e150ffb548)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/7bc1efe6753882268a54882e30c0ccb46f5fa69e)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/68defb93620fa154e8f022a0cc699c532745a753)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/97fde55fa035aaeb01d4f561473a692583f19d29)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/fb3b4b8202ba513246a347f223e35c8f52dc5b31)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9bd20bd33dfa29d7f2734f15fd100ed12864dce4)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/9bfaa162e747fd38e7ba754c8db7757167720456)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/dfb5c4b698f0357e0d5a92fb54cf85450e1bb8f2)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/2d4e241b38874143f3db01fbb0baeb51ec17b22a)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6fcd11d946a1ba59d1c2623174e9c9a0343be3d2)


## [2.0.4] - 2021-10-24

### 🐛 Bug Fixes & Minor Changes
- Fixed crash with certain tag names by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d048604084d449274fcb3dbea3321cea9a71dd60)

### 🌐 Translations
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/cc83e24847ac127f160cf7a311df4980229df370)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/625596b806d5237cbe1119f256c59bd29b7cfaf2)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4bed28759de295231f0450c697fbecd9fd5a9ac6)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b83579fa90c0de710ca90c59bcf22aa941308f06)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/9d4694622a738ea6ea67db82f41a601f5e4d7fa3)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/214dff3981f915032b582ec38a23b92871c6a1c3)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/481dbcd5e702a168bbe7700adb0b1f1fe6df63a4)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d46b883b7a24a09e2918a8e3c60406990120adcd)


## [2.0.3] - 2021-10-23

### 🐛 Bug Fixes & Minor Changes
- Crowdin integration removed by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/6f5418e3aa7ccfd337872f9d465ad834246bc0da)
- Correct locale for Norwegian Bokmål by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/826a18dfffbb9408614925daab28179654c6af78)
- Reworded some strings by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/871143e8fc740729b166b0fce68f6a95924b1531)
- Added a new feed option to fix feeds with bad ids by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c4048f9f0a96cb73b4d93ab0873ccb7b71ec9c3e)

### 🌐 Translations
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/eaddfe7eb12f7b6acab9be33ab902616793069b1)
- Updated Greek translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/99f8e3331f67a6ecb527695dba6ceed1db694ad1)
- Added Telugu translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/28fcb903a634c9233f0c6d08b8643491eceeac07)
- Updated Norwegian Bokmål translation using Weblate by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/1612b7c227b8fbe7bde4781a008f9c705ff1b0f6)


## [2.0.2] - 2021-10-15

### 🐛 Bug Fixes & Minor Changes
- Fixed back button not exiting app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0dce0373f0812c86af86ad007fcdcf01beda0bab)
- Added Compose test for back button exiting the app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/47e48e547d6af11f2110f068326728119ed77a9e)
- Fixed some broken tests by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bb113d3308c5e9807d8c71946bb6928b933207c1)
- Disabled broken sync test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/20517a0207bedbfec183561effcd3ea6709b7a38)
- Fixed scrolling in Reader not working close to screen edges by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4b3e83f9afa0018253329f5e91e5ff363000fad0)
- Fixed crash if loading very large (50MB+) images by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7eb2d0f81003a392aafc614010f1856e42cddb78)
- Fixed black theme not having true black as background by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/941b3341d86510f4cded45b3936cfea46700e2e1)
- Hide FAB when feed is empty by @fealvarezpaypal in [commit](https://github.com/spacecowboy/feeder/commit/aed89f363149f6e5fca387280330ff1b4efc874b)
- Swap booleans order by @fealvarezpaypal in [commit](https://github.com/spacecowboy/feeder/commit/3154cf7a30220dcd833fefd67981ec5fa0b6a455)
- Fixed Open Items By Default not defauling to system default by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/96d13aeff76349479756050e01afbf920ba49f62)
- Fixed crash when adding feed with empty title by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/31851c941a8a28af85de6ce7ae143b5f3ca6334f)
- Change RSS ID generation again to avoid some duplicates by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a74c8664a0a93e23ac3fd35ebb0f1ba8fc58c816)
- Fixed links always opening in custom tab by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dca107a143c5c6e916a715dc917da86ebaffc2c5)

### 🌐 Translations
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/02c90a8468f92e411763d709971602d1df01dc19)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/2530f0749f578710b8d051ef0b83cc08abe5259e)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/972a4a757bbc4900690b2e8a31344c420bf00e2a)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/6b8ec48f23b1d8d0058823a347ad304b4c15ca7e)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/99457a1a35e815d03f562279a8d50113912700d3)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/02d0b027483c5ab9c133c747fb3f272430d49f9c)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/0bd119ca451d29933df3783b6175a61fbf1c6467)
- Translated using Weblate (Ukrainian) in [commit](https://github.com/spacecowboy/feeder/commit/dc7b2c8fd777e4523824c9c64823cb65edff25b6)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/83b12cb6f696f658b80b1bc4dd34489af8486996)


## [2.0.1] - 2021-10-10

### 🐛 Bug Fixes & Minor Changes
- Removed reference to deprecated kotlin-android-extensions gradle plugin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/47f5171b2b25ab04870581e266a0d91eef66ebba)
- Fixed reported nullpointer exception by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/204f854d7172645350b0deedc70a71c828842448)
- Fixed enclosures not visible in Reader if it had no name by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ea9fc2872602a473dab64efba99af68d0aa423ce)
- Fixed possible crashes in semantics by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7324a25c3336db525f8630ed1cbb3689535973ac)
- Fixed possible crash in play store by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3d21a70062cdb2d69c7e4eaf400862e2b847dd6a)
- Fixed tags with certain characters not working correctly by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/81b81aa14363afe3ebc9c8a4b0df07a392adab9f)
- Improved a deprecation hint by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b8334fcaa5333f59bc8298a403480ac5a70e61d4)
- Added a search button to the search screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/63fa3b542d974e83fd316df0f2d742aa8b54044a)
- Added share action to long press menu in FeedScreen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8b860ae976f03d90f41d8d74d35709fe22543027)
- Fixed handling of ids in RSS feeds where guid is not unique by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c896bba0e4cdfab60c8a6d9a31410ad0c0d489b5)
- Fixed timezone sensitive test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0b3470d1a5222e8d3274f5486c7956a52f491d47)
- Rename dark theme by @fealvarezpaypal in [commit](https://github.com/spacecowboy/feeder/commit/d013b98ad7d8f3fce8dd88d2c8e5c6bab24e5e0b)
- Add new theme by @fealvarezpaypal in [commit](https://github.com/spacecowboy/feeder/commit/656a276a8b744fed0bac06c7db9c1d5f2ba7989a)
- Add dark theme pref option in settings by @fealvarezpaypal in [commit](https://github.com/spacecowboy/feeder/commit/901097973e67950fd9eb5271af3ff552751ded4d)
- Pass dark theme pref from views by @fealvarezpaypal in [commit](https://github.com/spacecowboy/feeder/commit/ccd50666f3abb0342370be61b862ad630cf288c5)
- Update unit test by @fealvarezpaypal in [commit](https://github.com/spacecowboy/feeder/commit/b7a2a117ca2a6fded6b0f563f11434527bed509f)
- Fixed failing test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9b0703847303f219e567412c2f02976125920ec9)
- Updated swedish translation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c95053ef8014884c458b125105765db753b827ea)
- Update dark theme string res by @fealvarezpaypal in [commit](https://github.com/spacecowboy/feeder/commit/e2b3f63a5c5a5fa6c48c81567d56db22ccb109fc)
- Add spanish translations for dark theme preferences by @fealvarezpaypal in [commit](https://github.com/spacecowboy/feeder/commit/b9bd64f5f6b8b5bb318c7c1579bbcf390eb2f621)
- Added test reports as artifact to test CI job by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e95815230c014be8cb8fa52809359c74698b935b)
- Removed unnecessary flaky test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2e57a1cb0208c32fa67507cdfc91bd7401ae1404)

### 🌐 Translations
- Updated Portuguese (Portugal) translation using Weblate by @Nockkk in [commit](https://github.com/spacecowboy/feeder/commit/d4a8d9ffc74fda34737a3bdd85ea19603639afcb)
- Updated Greek translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e688797f3fc113fa5bfaba697a78bda849f3ba16)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/25966dc223d218e78127cd54d40664504185f16e)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/18fcb357d5d554a81c4da54c210bb1bf8d78ff0e)

### ❤️  New Contributors
* @fealvarezpaypal made their first contribution
* @Nockkk made their first contribution

## [2.0.0] - 2021-10-04

### 🐛 Bug Fixes & Minor Changes
- Mitigated that selected text can't be unselected by tapping by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0f94bfc6cf60a1f00230244fd67aa4b3b11cf8a8)

### 🌐 Translations
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9ca9365f2303d0c07039243839dbc07ad7d41e05)
- Updated Persian translation using Weblate by @ahangarha in [commit](https://github.com/spacecowboy/feeder/commit/617bd48375cd881188d52e11f7e68846b5cf0b8f)

### ❤️  New Contributors
* @ahangarha made their first contribution

## [2.0.0-rc.4] - 2021-09-30

### 🐛 Bug Fixes & Minor Changes
- Use BoxWithConstraints instead of onLayout Callback by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b9a627d43f502cc6054ddaa5c7dfd2ef863aebe6)
- Increased swipable thresholds to mitigate mistaken swipes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f59c5dc50a238e296a3b3e3efd70b71fc4e00fca)
- Suppress some warnings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d4138a7a48bddee629ac25823ece6e9dea63664d)
- Fixed so list scrolls to top after mark above as read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d0498ff0417defb633c614e1f5cf2289d1389555)
- Fixed sharing article link by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6c96cdc2ed34c640465699b9e0bcd73d53102e3e)
- Fixed customtab/browser not marking articles as read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/777be5972327ebc976d7638ec3a1c850fd1b55ff)
- Moved SearchFeed to own package by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/980af280e7ee21944d73a6969591340ef8bc9bb7)
- Made UI not so wide on tablets by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4e87becfb2f3aeb6bd7b95cf4248f34af3d2f523)

### 🌐 Translations
- Updated Chinese (Traditional) translation using Weblate by @mixterjim in [commit](https://github.com/spacecowboy/feeder/commit/8a252911204fca1e212542d9a9ebd3a7b796d441)
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/f4e8714522a7453d3c2d859349e3ebeddc7d4c3c)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/34edcbc3401f72efe1b606679c6a7c210f504aa7)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a032ba032548c8f203c9e12a092bad2ba0331958)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/89ba5aeff68ef7510f02292dc79229332c796d4e)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/38b0167bb2695281acb7dbdf29f4a97e27736028)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bcfa2e7b13c1a9be8005f78bca57aa56c406d853)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/3157b54ca84b8150b88435d0c4e52adbd3cf1456)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9708576109a85001a49b5394c1017059bed8cf6e)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/1e442a3c3f5f2d68349f81ec8fa9d6593ca72272)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/12b100a1bf00aeb03d8cf3074fd454ffeb17af9e)
- Translated using Weblate (French) by @VeryTastyTomato in [commit](https://github.com/spacecowboy/feeder/commit/8780fab42a542114a7f2927eaa781254b827e405)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9b3e4bbdf9e1f0ac788c5dff629a5efac813fd45)
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/991f1204ae952b031d0f24ccd4372a9ba65193a9)

### ❤️  New Contributors
* @VeryTastyTomato made their first contribution

## [2.0.0-rc.3] - 2021-09-22

### 🐛 Bug Fixes & Minor Changes
- Minor fixes for better understanding by @jampetz in [commit](https://github.com/spacecowboy/feeder/commit/7c8bea2541562ce41f8df40b21357e841020240b)
- Refactored and simplified view model code by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ea09412c49eeac450858069287475b379a0e0681)
- Fixed infinite loop issue if for example a notification was clicked then back by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5b0892ff3f0285b25e1206665b025871a80ce997)
- Fixed app shortcuts not being cleared after delete by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0b35343c5895bce063ad31f661f5c01e1ec87737)
- Fixed TTS (and rest of app) not working on Android S by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5b0badf831b74031c66a0debc3a657e5829b8d37)
- Fixed incorrect imageloader being used (should be a few percent faster) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0abeaa0b78ab20a2a66db707eccf32790e3947f4)
- Testing a card layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/abbc8bdbcc34c69c2dfbd67b5a9743d3ce0b74bb)
- Fixed image placeholders by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7afb10830338ad0d977579c0fd43f50fdd00a01a)
- Tweaks by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6a50a07f1004631efebb65c4d5396b7f80bc7aff)
- Fixed swipe to dismiss by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/604c09ef7050a5d27a174102d9fdb4c2d8ed6b6e)
- Fixed images in reader view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/95118fa69e9861c04d6cfc3fc3317883020251cf)
- Tweaked card layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f0c77ede1dd349eb3139bb7d94ff19c356a4c6ed)
- Added a new setting: style of articles in list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4669d4d36d0a137a90b4debc2002d80aeb3eb8da)

### 🌐 Translations
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/16520c88d301134726256db843a939abd26eb6b7)
- Translated using Weblate (Ukrainian) by @jampetz in [commit](https://github.com/spacecowboy/feeder/commit/e2cd1132b714fcb341d6f0eca898f9d4bb1971ef)
- Updated Chinese (Traditional) translation using Weblate by @mixterjim in [commit](https://github.com/spacecowboy/feeder/commit/12ce17fd7c2bd97514014559d0c90faffc912229)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/575e1dc0c351e0ab73676f2e8824396829953de7)
- Updated Ukrainian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4615622fb9c9778cd04bfb8c2a8dbb62f58138ec)
- Translated using Weblate (Ukrainian) in [commit](https://github.com/spacecowboy/feeder/commit/02f6598bc9b1760482b6395211d14f49561911fd)
- Updated Chinese (Traditional) translation using Weblate by @mixterjim in [commit](https://github.com/spacecowboy/feeder/commit/7ff61b37afa2e94791e7486091d5b07faa39a18f)

### ❤️  New Contributors
* @mixterjim made their first contribution
* @jampetz made their first contribution

## [2.0.0-rc.2] - 2021-09-13

### 🐛 Bug Fixes & Minor Changes
- Fixed so TextToSpeech is not initialized as part of App startup by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d94de6a711f934f37c96285302bdd34df2c6c611)
- Added plural forms for n_unread_articles string by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e717ab311cd70b5773b9aa50e8623293da0a93e0)
- Changed so CI pipeline builds APK with R8 optimizations by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fe8faac491dfe892ae267a5e6a77d453976f58ff)
- Slightly increased size of title in list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d47927438289b7c970c6680a2cd41bfc7148da50)
- Fixed so read aloud player is not behind navigation bars by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3e00725fa1da1fa24e3bad370ae49d34e262e041)
- Fixed inconsistent behavior with different sort options by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/302da01ae957700a92fb1d40005ee90b9612476e)

### 🌐 Translations
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/76e9eba5016dbcdedfc08bf71196355ba25fdbff)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/5c5a3c911dd027e685d72c141dfb7c57b2f803e0)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/b36db45a4d1a1857d6b08bd15b99aff5e0323835)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/214e66605ca705243a1cced3a287ab4dde44518a)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/03f2be20005c6e12f8565d5b694b8be15f143270)
- Updated translation files by @weblate in [commit](https://github.com/spacecowboy/feeder/commit/14c1e1179614febc0c31b7d6aa082bcde485874a)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0aa392503c47466175e8b69aa7bbb1d9a1e1c300)
- Updated Lithuanian translation using Weblate by @70h in [commit](https://github.com/spacecowboy/feeder/commit/0d8028556aa4bdc7aa978f1c314010d329efedb5)
- Updated Vietnamese translation using Weblate by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/5d3998ec6d5cf40167c01b96ac58acb665babb57)


## [2.0.0-rc.1] - 2021-09-10

### 🐛 Bug Fixes & Minor Changes
- Fixed accessibility descriptions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f0e87b8ae0d623307d0abb4083a53051c1047384)
- Added an editorconfig file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c5e88ac6ab805b0d2f2c7729a94a0ff0edcfbeeb)
- Removed unused code and cleaned some things up by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e8e1ac7922898cf404eac03e8a6f2feea5b70b3e)
- Changed to Readability4JExtended for full text parsing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fa632512454bd0d117d952e06ca0c28e8782b85f)
- Fixed some flickering introduced by the cleanup by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7d50240356da1af80d53dd9a0010da3f4ed1a6bc)
- Removed out dated tests by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/52326be31af02bf7de72fc21fa40bd8ea969b16f)
- Made Feeder very TalkBack compatible by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/908efc980b2e414615a7c8afcb6350107ee4123d)
- Fixed a bunch of TODOs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/046b4eb710db3effd78c5e3089820f66754b5fe6)
- Fixed so Feeder handles rotation gracefully by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3fda90924392c126746c4869d8545ea01ad56505)
- Fixed test compilation error when targeting Android 31 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a215942a7599eab2bb2305d25ace0f63766fbec1)


## [2.0.0-beta.6] - 2021-09-04

### 🐛 Bug Fixes & Minor Changes
- Fixed sync indicator being rendered behind top app bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/08760e632cd229f9f422075afd4621198e3c26f4)
- Added ability to toggle between full text and included article text by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/92e802b0cd58c3e3f342c530782eab9756696168)
- Fixed so scroll to refresh works on the empty screen again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e46f91ff5f1f911db1d7c611185fcf888250921f)
- Adding some fade in/out animations to empty screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/47c40796ea6bf6269b76b836f273d30625cdfe0b)
- Tweaked some padding in list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d1275492869ef5f764f9b7d26ac57ff30fc56313)


## [2.0.0-beta.5] - 2021-08-28

### 🐛 Bug Fixes & Minor Changes
- Fixed incorrect decoding during parsing for some feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0ad40b9e0ee95a2c293574a4b08420964676b5a1)
- Fixed color of icon in floating action bar to be white (again) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d0b6ca47292875c9d7b6eafcfa601e705321a540)
- Made 'Mark as read' from notification less interuptive by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fe52639e1e36f225b46aee9bc681079a508a141d)
- Fixed broken test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7c24c763b4dd19c66600722b2c8a2a51c650a3ce)
- Feeds are now sorted alphabetically in dialogs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/78da415a083dc44c9b9e5ba67c08b2b2288934cb)
- Added dialog for editing feed when viewing a tag (like for delete) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1e2fe64711258bcd1851ef244d3a7fa55bc6cb1c)


## [2.0.0-beta.4] - 2021-08-25

### 🐛 Bug Fixes & Minor Changes
- Reversed expansion icons in navigation drawer to match material design by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fdb700fa391178baaefd526508d9673aab8fd4b7)
- Fixed youtube thumbnails and made images clickable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3e6ce929f7f3ab9df4cbdb001cc2b0914970abf1)
- Sync on startup if set by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/56a6e4647ec73dcec697d46ddb74d10c9b78a84d)
- Fixed instanstiation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2db6af2306427298df690821adc84cf354e94c27)
- Feed Title clickable in Reader again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b7e74a11c1aff398d99e62b3a86236614a24c477)
- Fixed color of status bar and navigation bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f5ecd777a80d54631cb3f67fd0ee4aee2255c910)
- Fixed padding in navigation drawer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/937d9e3f963b31d3037e7aedf0d3ab1ce2e9b4dc)
- Fixed theme on all activities by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cb9855d4c4a4a4cff6de4d6df98a3f05a91a297a)
- Fixed toolbar color in custom tab by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f975fda2f8b19f31b57a56864837e0c51ee4a9c5)
- Fixed color of icons in status bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d3ca1ffbc407a6b8d6ead23d2e2cd787b3bcdd29)


## [2.0.0-beta.3] - 2021-08-20

### 🐛 Bug Fixes & Minor Changes
- Fixed some notifications not being cleared when opened by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a97a0257675892fcacb806fe03e7e18bc9c9ea02)
- Fixed feeds not being possible to add after enabling R8 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3f8240f35956219b594878777e788d3ec5a944ce)


## [2.0.0-beta.2] - 2021-08-20

### 🐛 Bug Fixes & Minor Changes
- Renamed Norwegian play store metadata by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/60694836223d43182f5155a1cdd310516b523ed1)
- Validate fastlane deployment on 2.0.0 branch by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9c8f5e078d8a803c57614bf8535585a00c880427)
- Enabled R8 - compose relies heavily on it for performance by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/aea5857876912e15370eeed2418a933cdb8bfcd9)
- Fixed mapping directive in Fastlane by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3d2aa645cbfdf3d689ab5bdc8c1622d0c557573a)
- Added beta support to fastlane by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/93b8f4c8e16d054db5c62705cbe6637e12218a63)


## [2.0.0-beta.1] - 2021-08-19

### 🐛 Bug Fixes & Minor Changes
- Just get stuff previewable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b7e81068165ec6afdf57c76346ea81a9f7580ed0)
- Outline of navdrawer items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7be6533ba0b9d77d049f1dbdec0de0b366b22656)
- ConstraintLayout used to implement expandable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4cfb4556f1c0cb3b5bb64c98013f0e3cf927e5cf)
- Navdrawer running as compose WOOT by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bde9abb0c357b126f182a55fec3983b23c998470)
- NavDrawr back to lazy column by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c4120056ba9e02afda5d0d07a789b02d255140d2)
- Compose activity showing drawer! by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4c5f82e0c4fc5df04dded71cd0122d2de62285bf)
- Swipe to refresh and empty screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a219578c8c4850dc70b9fdfeaabb2fbe84b1e1c8)
- FeedList and EmptyView by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0480d61f4f46cb2a1ebb4cd12c3806806998d4d6)
- Filled out feedItem by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f029bc5e044b4094b848b01367e42bf7d7451e10)
- Move things a bit by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a8112b89af03fad818db05291fd5d082f774b7c9)
- Navigation to reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b9789d16789b32cdcfff019acc3883e586fe8cee)
- Hoisted state by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c2732408544fae632e3efb61d41cd74da28c5906)
- Updated kodein and added kodein compose module by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c141ddee836cb2b72ebb3809cd0e679891f78570)
- Latest beta by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7df6d199dd78e6bd012c8e3785da713691dd92b0)
- Fixed compose reader - and exposed strange bug in spannable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7ece099ab59b353a6a5300a3847f058fe44e0584)
- Animated child feeds in nav drawer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0cdcf754d631e7ee172c28b17a13d99b06b24298)
- Fixed total unread count bug by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dfa5ba9a9a1018718f43390b2292549b45bb75ac)
- Fixed spacing in feed item by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2fe13734336e0623fb86ad20bb1e29f8b7d9e254)
- Use image loader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/63ec0d62f6e7d089827e83351fc505ca83ff02a4)
- Added a delete dialog (not done) and overflow menu by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fb81bf984f027673481a226a6d86e4a0d1dadcdf)
- Removed unnecessary experimental and preview annotations by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/56475f3eabae53c82e6bc8baa2a57a2f9fa1a2c0)
- Revert navdrawer in old activity by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f5051212d41acdc24eedc29347e8eb3e493fa6d9)
- Finished rebase and update by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a7926ad7d11b2d8063a7384c7688dc3dcd84145c)
- Composable reader! by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e931dfdbc6a793fd0cfb8cf416995a078f8d4546)
- Reader is now lazy loaded !! by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/579e0aa3457519bfa0da67eee56ae776362ab698)
- Toggling unread works - still blinking in list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bf4bffcc7fc6ff9e0a464e39f9ff0f69cea8e195)
- Caching it fixed duplicate emissions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6891267a208330e1c3fa63589fd031d116c3d96a)
- Got saved state in to handle desctruction by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/546572de3753d2de07b1b7ceb8945356cee5d2b1)
- Fixed missing code by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8bbe31cd08c826728fa2c40aa37ca716050213a7)
- Scrollable code blocks by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8e5033608d14d8e1368e77074d1195b8ef2bbfd0)
- Delete screen works by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ee5876941d0e3636a3d08378013067af40ac40c6)
- Edit feed screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/27c32fe8d9e8e15f70950744410677bd65de0770)
- Replaced incorrect usages of rememberCoroutineScope by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a5fc7634dd1813795278c985a4c108d8364d30b3)
- Added icons to menu and simplified rotation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1abea37294dd4bdb76eb8429204f03ab0b611060)
- Removed old activity by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/518a0216bd4fbfe5c7842a39021f4583049a8980)
- Built the settings screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d4a7ef14f1eee21dd5e379876243c92ddbe88be4)
- Theme setting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fa6905ab30cae72eca1b3cc992d8822fdaece55c)
- Fixed bad translation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/01fa1bbced5990083f03ed901f9bd95205eb9925)
- More settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f34bd506444a3deaa488b24fd46434c25eb5497f)
- Settings done by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ce69d94fa0f34933535addcbac324b9f35707479)
- Search screen and add flow by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a4cc4cdb82d6e1600a2767c93fd9636f924fc73c)
- Fixed margins everywhere I think by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1691b7fdbc15f2d1a69a09937da2725ab497efbb)
- Absolutely keep the spaceing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d299480c98cfa3dff43923333427e4de0b75264d)
- Increased size of up button by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/050033ffee9c28239285b93e6a1d5a0f6dad5944)
- Did it correctly.. by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eb33350cd39ddf1c26b0c5b3a272e01294ab4a7e)
- Tweaked drawer layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8989d09a99f1e7aa5265dcb758c206e318535556)
- Removed contraintlayout from drawer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bbffff0f66e4b771f59af46df9f76c24e8ba4a04)
- Fixed text color in dark mode by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/590f316e5a6ddda9d715e46947d318422148a0d3)
- Fixed persistence of theme value by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/919791b486290947dba50581efa74472b6a3037d)
- Fetch full text action in reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7b0c320af2073409562bbb64b8aa2869c0a02f9c)
- Clickable links and such by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0ac52c2846060d9e4bddc3e34ac545bc60a7c00e)
- Updated to latest android studio by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d17fb273d2e201d9cd595c0b6c0f85b5712fd485)
- Fixed dynamic height of list items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5f76de4074d0412c49036089cae16c682b507700)
- Typography and block qutoes are nice by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c6e62fdbb656a8ca070ad332e9f987e3459ab40b)
- Some formatting tweaks by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1f4ccfb08f923a6fc16260deaad5152e223f65fc)
- Blockquot style looks nice by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/019ffec9151cdd4ccb27d597d090a819318d0777)
- State hoisting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/15a7d4f6412d4428a5a059ad3d573e1c4480155a)
- Fixed padding and some alpha by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/98f2f95a4d59552821d49cbddc59ea428560936f)
- Zoomabe image works - but scrolling breaks kinda so disabled it by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/25ed7f63f5f0ec20711e2e5402aa8b6f338d0a2a)
- Fixed some backstack issues by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9689ae60498c56015a28f2e587fbf7ac7246e187)
- EditFeed focus by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5eae046f49f92d6fa80ca36603c342f7bb10b2bd)
- EditFeed auto complete by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/48cb6915bf23f43e2e58cae550afddf52f272ff2)
- Fixed readability4j issue in parsing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/388eff36e8571522d1f8bf1ee456b3426903373c)
- Close keyboard when searching for feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cf32558bd554f16539b00224224e7650ec2e413c)
- Update sync settings for real when settings changed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/85cba86c0519b003afce299c38228ef67d87e5d7)
- Sync when savee by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/abb8e950ce452e2113261ace88e4ec80cf707721)
- FeedList menu items done by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ee0c8cd035b69ef56386fb4352f2c2c21adfaf30)
- OMPL import/export and delete styling/scrollability by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/180d48b1c3bcf1e22edf37bff5881de8a997bf19)
- Share action by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f083233d1bc0f4d13553eae6d803af22a129430c)
- Open in custom tab by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c4970a22a82ab99a2c298cee05af7bf87186a2f8)
- Unread indication by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/304e751b7cde5180da4e9f94df636cb71e8e244d)
- Feed items now refresh correctly by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3eb24ddd659dd96a3fafdbbf60c471db84492222)
- Disabled is too hard to read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a37fbf969c014efd6f1181c0d521d9ac395a9d80)
- FAB by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6ae1814a7b9fb29e8d0ed98546afadb7d0a04b0a)
- Size in NothingToRead screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/771e3628b943813bd26b050c7c1871b814f355de)
- Respect FAB setting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3b5473be48217e16be393093567d00ac18156a5f)
- Dynamically update unread counts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a964c64ab9a603281bb5595a665dcdd89684ba78)
- Started on dropdown menu by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e70f9358227f194fe433ad07e5332f85d4e46bbc)
- Finished mark as read menu items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/145683bdd40d9846416fccc5f86094112ab7313b)
- Title of screen and sync all by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7210c497ec84aa5a459dd78e9867d534f28b46f0)
- Updated to latest versions of compose deps by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/536ef3142e0a02d241cd029dd8e2e26e9962cf6d)
- Updated kotlin version and Room version to fix compiler issue by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d96619d2441e3be2a751261f8ba958a4f2b22346)
- Updated coroutines version by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7607eae158c6a0a4419824d3f38b181f910dc173)
- More stable list sort in feed screen during updates by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dba3c4a4ac4dd0d39059e219691e5807fc823025)
- Respect open item with settings on click by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/21983e9cf54a4c88bf9bbf97a7c0915d4bed5aa9)
- Respect image settings and show theme appropriate placeholders by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/92685b0c44ab8f9b9a5ffb9cde34c2a533504cb6)
- Removed preload custom tab setting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/086b82f773e6eba562059bea01a7012fc3f5e001)
- Added handling of external intents by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4af15caabe654b01e4a6af6d8825b77695707e16)
- Fix buggy intent state by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/79df7e451a7ede2075af5949f12dab0991be7de9)
- Updated to compose 1.0.0 and latest libraries by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/24af5961570bc3a38c5dbe66c70faf1aea5ac492)
- Made text selectable in reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f059a1e0e1f1a60b021924145238f5e0f807d271)
- Added some bare minimum table rendering by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1d5e310d567cb43ca2d32616b4ce46b80763079c)
- Added todo about fixing clicks and deselection by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f6ee04cd6c79c134fb98e333924b95ace49987ea)
- Implemented swipe to dismiss - but this broke swiping out the nav drawer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0d756627e7674f16f586e0200b52b39a3d68bf4a)
- Put other approach to dismiss in code base by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/629900ea1c1b6e10918ec62c1a9cf5e7a4198068)
- Added a todo by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/15533c9dc40a034d74b15af3a3d3bc3303645e4a)
- Fixed size of swipeable area for swipe to dismiss by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4bc1f59b903f14ccc9c1fde3d91bbc37c854cdf1)
- Fixed title of screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2bd0d76c0d1dab825ba3226ad45634a028415bbb)
- Fixed order of elements so swipe doesnt prevent clicks by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/827a69aec6194ba128c8b0153710513a62e6bfe7)
- Fixed swipe preventing clicks - nav drawer cant be dragged for now by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7206bd13a3a1ae288b18edcf2e473c3254001776)
- Implemented read aloud with a playback bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d346f02b20751affcc1bf64964e59f5b31d4994e)
- Fixed some things by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b67729ee2aa75b666c786be4d6bb8949cded0346)
- Drawer is draggable again! by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c26036d82bf6b185066131d014c700414a64372c)
- Increase gradle jvm memory defined in project by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/335162cf2ddeab458a0d240ce7e2e20a04648c84)
- Removed unnecessary experimental opt ins by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/acb6f015e8d5738ebc1f8ec43bca737c17020504)
- Removed failing test for code which is going to be removed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0f3216b8b9a6af8b94569e88a22c347dedc49344)
- Fixed issue with wrong object having spans popped by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f5928930f57f2adc6781369e3742ef0dacb64783)
- Fixed a crash in reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f3e6b898c9fa989ee494d9ac4d6365bcbf7e80fb)
- Fixed broken scrolling in Reader for some articles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2a697bf5ac9581392b2ea20bfbf47f71d0d6fddf)
- Drastically simplified intent logic by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b067a72aabc7ac604b68059f3ace545693cdd891)
- Deep links for app shortcuts - back stack is wonky but it works by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d0ab7878d68e6ecbcce8115395d3a0ae12775014)
- Updated deep links in notifications for android >= 24 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8b4e58b335fd12e7b21dd7a7398f45694f640d42)
- Fixed notification actions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ad661878be6059a260a2124194ed9a0636cb10e8)
- Fixed some backstack issues and newly added feeds should sync on save by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e5af6c797c64a3ff1d288115d922ad8e47dc3841)
- Fixed menu not closing when clicking on menu items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3a0c0228f7266d7235daedf61b65998115a3a805)
- Fixed last notification deep link by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1f47dc72b27d0437416ee1af565ec90119e6842e)
- Removed useless file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f376359c233daec16acb2de2271ee563d23a0dcb)
- Updated all languages to use annotated string instead of HTML by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9aac9328e2ab744f3482cbd4ff5de2b70f4a477b)
- Clean up gradle script by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2b68d44dd0523f549e1d4e163a6ee390e575b1bb)


## [1.13.5] - 2021-08-19

### 🐛 Bug Fixes & Minor Changes
- Changed so Feeder no longer changes the URL of feeds to canonical selflink by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b9c9779762e00f757b53e0e901a1f13ff3c65520)

### 🌐 Translations
- Updated Swedish translation using Weblate by @bittin in [commit](https://github.com/spacecowboy/feeder/commit/850f411dda2678d4b0ec148d2f490bf95b18a101)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a01dafc7e9424c80aadf922dfa70244ea60bd16d)

### ❤️  New Contributors
* @bittin made their first contribution

## [1.13.4] - 2021-08-05

### 🐛 Bug Fixes & Minor Changes
- Added missing title for language lt by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b164dece3728411c1625f60c50756bd1275b6345)

### 🌐 Translations
- Translated using Weblate (German) by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/2ae6083227a9eb08eccd2e6a9ce78a08c785cd43)
- Added Thai translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/18a39e6358757af737f7b09ed34f887442113336)
- Updated Vietnamese translation using Weblate by @84436 in [commit](https://github.com/spacecowboy/feeder/commit/3d4cd189a5c0f083d38623d7c145b2bc7848174f)
- Updated German translation using Weblate by @daywalk3r666 in [commit](https://github.com/spacecowboy/feeder/commit/7a22bd67ac2c3da0e0a7e7383183deaff3d70ef0)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/368fa8ac545893d26a72fc819e97b07ef3d93d71)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/098172c1831097d549a31af2c04d82f69305e3a5)
- Translated using Weblate (Lithuanian) by @70h in [commit](https://github.com/spacecowboy/feeder/commit/8a3df04f94b556557c8b9db95f40e80446334ec9)
- Added Slovenian translation using Weblate by @weblate in [commit](https://github.com/spacecowboy/feeder/commit/35327ceabf4ade46c05db78ef6053302e62822d1)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/d8455440292a5db0ca35a671c04df82e7932f947)

### ❤️  New Contributors
* @70h made their first contribution
* @84436 made their first contribution

## [1.13.3] - 2021-06-18

### 🐛 Bug Fixes & Minor Changes
- Improved formatting - should be less empty space and newlines now by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/199e8bf6d530ef90bef493b1baeb7df2b5fa5cc3)
- Fixed test broken by formatting fix by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4f9450c575882585b731db64c54acf3d7ed52b1c)
- Fixed folder names for languages by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5d9e4ac38ab8e93a569e0713f0ab6f721f9a6a0e)
- These folders were just missing region flag by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ae3d47eeff2c928c46c0eaf363b65d2313f56918)
- Fixed folder name for Tamil language in fastlane metadata by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0edc2af87251198b19fd0197cdf32be8afdee431)

### 🌐 Translations
- Translated using Weblate (Hindi) by @nikhilCad in [commit](https://github.com/spacecowboy/feeder/commit/7bed6c84d05242ff2091707f2d4ac15cc20166ad)
- Updated Polish translation using Weblate by @Aga-C in [commit](https://github.com/spacecowboy/feeder/commit/029f7af48d0e640b7acb801439559544d67c8075)
- Translated using Weblate (Tamil) in [commit](https://github.com/spacecowboy/feeder/commit/eaae183b1dd8bc5027e2c6cf01d442dd416e53d2)

### ❤️  New Contributors
* @Aga-C made their first contribution
* @nikhilCad made their first contribution

## [1.13.2] - 2021-06-13

### 🐛 Bug Fixes & Minor Changes
- Upgraded kotlin to 1.4.21 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/14d76669777f75c006f32f2ed7197246bc457785)
- Upgraded okhttp to 4.9.1 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6e5e34cb29b01ced349dadef78daf6c174d61709)
- Fixed some version conflicts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b46d0d9c9c1a075bc4ed4ad9d8132e547b5d27aa)
- Raised minimum supported version of Android to M (6.0 - API23) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cadaef03302f115de2285ada75f5d387a9ab7374)
- Added support for TLSv1.3 on older versions of Android by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/df11985f559a867dcb29fb2e1764475a65fb32d5)
- Excluded junk from packaging which only conflicts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f664611eb4fefc49a03a4db53b6807bf56bfa1e7)
- Split pipeline into several parallel stages to reduce required RAM by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/37c21893b3c2cd26881b86676e30647baf3b3644)
- Fixed fastlane after upgrade of Android gradle plugin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c0a5bcdec8297ac5dd4781e30cce012461c2bef8)
- Use different colors when swiping to mark article as read/unread in [commit](https://github.com/spacecowboy/feeder/commit/c4545c2b6264029175a0ab8e7f7cee9ba46e06c0)
- Update README.md with ko-fi link by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/40549eea09b9675f9f48d1662587c85b3593b0a5)
- Update README.md with play store link by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b0fb8bed5a73e349d5f8d46235cef27b69efeb97)
- Update README.md by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1d67f035b3029aa8bc96310e7d38cd500d3b9172)

### 🌐 Translations
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/9d750135e1ed445fc50c8dc8836ca02cee20446e)
- Added Tamil translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b2422d25b150f791281f8779c414c7d34de7932c)
- Updated Portuguese (Brazil) translation using Weblate by @gutierri in [commit](https://github.com/spacecowboy/feeder/commit/7426f9d08819cff59c3cc5891ec9236f286300f5)

### ❤️  New Contributors
* @gutierri made their first contribution

## [1.13.1] - 2021-05-12

### 🐛 Bug Fixes & Minor Changes
- Specified the region of bare Portuguese to Portugal by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/533e92d427a873c3de4e557fd749e891ae4681b0)
- Fixed dc:creator not showing up as author in RSS feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2d17319618dd239c7bc122b44f4d3fb392dbdde5)

### 🌐 Translations
- Updated Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/ec3643923bbe41220df66fc4c49cc9de753cd87f)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/2b3e17bb0e6bca6a27bbdb32e907a077ed8ed51d)
- Translated using Weblate (Romanian) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/73e6cddbe0a6a1cc2e856667b4da86703003a8a1)
- Translated using Weblate (Vietnamese) by @unbiaseduser-github in [commit](https://github.com/spacecowboy/feeder/commit/8652f08722245929f0fa0a45cd00968866df18bd)
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/c8af81d8c89b9570466a8ce9740fe5d3bd3331ae)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/2f4d770fa6427f6b731a76f4f6006a9b3dd381bf)
- Updated Czech translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0a2452c56502453011512111f4788bf07b0056ad)

### ❤️  New Contributors
* @unbiaseduser-github made their first contribution

## [1.13.0] - 2021-05-02

### 🐛 Bug Fixes & Minor Changes
- Added option to set article reader on a per feed basis in [commit](https://github.com/spacecowboy/feeder/commit/64512d3a1e5d6105e761ed1c91c7d0b2f49e194f)

### 🌐 Translations
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/b351658fd54b954195c685294ca520df322cd3de)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/e03eabb22488fb28b278147a0e48d74c3f0a4240)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/4995ea75136fd89224663c4cd85d6b48da57b4db)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/966376eb5515bdd05554b5c4497e369017af57a4)
- Updated Polish translation using Weblate by @WaldiSt in [commit](https://github.com/spacecowboy/feeder/commit/a375332b85ae7cbd0a865c090cf03cea2a4d3f43)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/add0ef71867895f4a97f1173ecf60c0a000ab427)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/fd48664d24e1a4e8695979fc16a2e942dac43e2f)
- Updated Esperanto translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0293dec5244a54c0e3185563e49c6a770313a41e)
- Translated using Weblate (Portuguese) by @SantosSi in [commit](https://github.com/spacecowboy/feeder/commit/46890329328d8735986af19b3c50d7b62875134c)
- Added Romanian translation using Weblate by @simonaiacob in [commit](https://github.com/spacecowboy/feeder/commit/47baaad11fef5fdb3035faf1236a964e41340940)

### ❤️  New Contributors
* @simonaiacob made their first contribution
* @SantosSi made their first contribution

## [1.12.1] - 2021-04-25

### 🐛 Bug Fixes & Minor Changes
- Prevent fastlane from conflicting on releases by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/67f53ebc7c701733e216d4e688681731c058cd1e)
- Updated czech strings in [commit](https://github.com/spacecowboy/feeder/commit/40adb64fbd1f48d799aec1267c57af44fa1605c3)

### 🌐 Translations
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/666f0e3cfadf39f3a48b7462407eaf86e8b9a01a)
- Translated using Weblate (Chinese (Simplified)) by @cld4h in [commit](https://github.com/spacecowboy/feeder/commit/95e8f6dfe4e4af2d8f84ae08751655a815efd2de)
- Updated German translation using Weblate by @VfBFan in [commit](https://github.com/spacecowboy/feeder/commit/92e2a2635105510a6f89fa88ff7094640490dbd5)
- Updated German translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/cccbf8a70d2ae00edb6ad0d64fcedade6ae54bd4)

### ❤️  New Contributors
* @VfBFan made their first contribution
* @cld4h made their first contribution

## [1.12.0] - 2021-04-11

### 🐛 Bug Fixes & Minor Changes
- Handle dynamic shortcuts for deleted feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/65a1d9b4cdca2ad6ef7cbfdb845743fa4dfaf7c9)
- Add support for reading feeds aloud with Android's TextToSpeech engine by @Upabjojr in [commit](https://github.com/spacecowboy/feeder/commit/6d51fd8cfb561e1cd6dbd42dfd54a74dcd0cfa66)
- Moved text-to-speech code to model-view class by @Upabjojr in [commit](https://github.com/spacecowboy/feeder/commit/770ce38180ea403a24c50f7f6cd937a6f6b32bfe)
- Cleaned up TextToSpeech slightly by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dccdfa0272b6ff808723fefc97a3db5bf603c858)
- Removed unused imports by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/187d22deb3f994d51ae4904e5a5b44f529dc4c68)
- Added esperanto to list of languages unsupported by play store by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f67bb1872555abe11288a6b1dd047fd51f812f33)

### 🌐 Translations
- Updated Polish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/1bb598ce2d3c147c2e88aaff47fdd3792c09c7bd)
- Translated using Weblate (Serbian) in [commit](https://github.com/spacecowboy/feeder/commit/c1e336fe55cb248eda211759cf00eba71c3bd799)
- Updated Esperanto translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a3302d9fe951aae2453cf7cdd4e4dfe8ce32c3aa)
- Updated Malayalam translation using Weblate by @Vachan-here in [commit](https://github.com/spacecowboy/feeder/commit/8260c4426e7339032fa8104b0d05e26b27be4ebb)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d491cd624da6ba4406b9cab2a478ad99a1ba426a)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/ddf06c3a95d3160c7174042b858e45042306077d)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/ddf53f410c3335f3e15106ad23fe3509b1612e3f)
- Updated Polish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/94b80f52ef7674f2d25e7c51a3684ce3b7503028)
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e2ffcab39e948900be23fe49c36ce680b0a04266)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/af708e46da919dd99407eaac3d3227427f1952c7)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/01d03869dd79029977775e836699a9c787c2b2b5)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/34e65ed7a42763d8644e5bdd22b97e8d439d1da7)

### ❤️  New Contributors
* @Upabjojr made their first contribution

## [1.11.3] - 2021-03-26

### 🐛 Bug Fixes & Minor Changes
- Fixed reader going blank after opening webview and going back by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/56b7c946fb746594d045872b8af1f5c04cdd5fcd)
- Updated view models with correct nullability by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/da0d2a9f3e19a27c0e88e684af5a1f3c48b583cd)
- Fixed additional fragment view lifecycle issues by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/078a486ca007437b2288b0e0c8da50f793651db3)


## [1.11.2] - 2021-03-25

### 🐛 Bug Fixes & Minor Changes
- Fixed sporadic error while loading images by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6023a40c9bece98b930af46e699dc64c0ff33d94)
- Updated gitlab ci by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4578f10b4b5d67640db54f519c41f658e2d493ec)

### 🌐 Translations
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/b5a50dfbac0fc07f8c8ab8415a317af50a26aebe)


## [1.11.1] - 2021-03-25

### 🐛 Bug Fixes & Minor Changes
- Fixed database test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/130137d3e6e9aaea04c8a801313b153d61018564)
- Maybe fixed a nullpointer error by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/aa9ebbd318c9202eb583b246543922125353d327)

### 🌐 Translations
- Updated Russian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d09adafe22125425eaf57d3d6f3165976e2c2690)


## [1.11.0] - 2021-03-18

### 🐛 Bug Fixes & Minor Changes
- Fixed links not opening after screen rotation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/074e85ac1e4f3d07fc0d4c21f81426cba4a1155b)
- Fixed a leaking service connection by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2bd413a7088ea746ae59ae1a081f66c80f348199)
- Update Czech strings.xml in [commit](https://github.com/spacecowboy/feeder/commit/ab41bf6e5aa8deb0d0b89a601da6f2de5d38a6fe)
- Added full text parsing option using Readability4J by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/13b0b6011a29592606f7346a275509e8bd764d20)
- Removed jcenter - except straggling modules by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d27fe805ecbf84732e3924199f68bd4e29bb9ee1)
- Ran KTLint format on the project by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ca0db5b9ccb5476d9635193641de99c796bdb776)

### 🌐 Translations
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/65079f61ed004e0f70231795c6c32b05d1a9a816)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/4af3761efb6dab1f0d5ddfed5faf499cba8d9972)
- Updated Indonesian translation using Weblate by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/5d7630770fdfa9b7eeb310a83c9127202c81b749)
- Updated Norwegian Bokmål translation using Weblate by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/4e05cb55eef7f7f75e4587d55e1842e5860ddf8d)
- Updated Polish translation using Weblate by @WaldiSt in [commit](https://github.com/spacecowboy/feeder/commit/b6ffe8bfc3f90b118bb0f29e417725ed5099c97b)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/e420ef8099955a0f30170bb01c82732447cc7f06)
- Updated Esperanto translation using Weblate by @Szafranek13 in [commit](https://github.com/spacecowboy/feeder/commit/d49aa9e6555c42b92d91a383874dada60d565c61)
- Updated Portuguese (Brazil) translation using Weblate by @eduardo2580 in [commit](https://github.com/spacecowboy/feeder/commit/7d23f022f63b769363c147fed5c36b03e95306f4)
- Updated Japanese translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/72b26b5906ad36cde87e82c081e5c5a410c76e86)
- Updated Catalan translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d36e6d6791e86ce7259993c8f17d516dea035f5a)
- Updated Italian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/d1b2ba2bd2f0716a0290f08125f791590732e5bb)
- Updated Spanish translation using Weblate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/5d6fe36de795fd5d980a1605f1913a985e61f59b)
- Updated Swedish translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e6a632d9c7a135256f9dae6c771cae76d64914bc)
- Updated Norwegian Bokmål translation using Weblate by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/8df0bd59fc27326756f6e06ce89b01a0d86d4f05)
- Updated Turkish translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a721b923d554e5faa57b018b4d18ff5c02375469)
- Updated Indonesian translation using Weblate by @rezaalmanda in [commit](https://github.com/spacecowboy/feeder/commit/90cd9031e2fecefdd9e391d02a80eff9e80b9c23)


## [1.10.14] - 2021-03-06

### 🐛 Bug Fixes & Minor Changes
- Split validation of deployment and actual deployment by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ea8d14a233e5808645a3feb691eb1945eb0d18d2)
- Added error reporting when trying to add a feed fails by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3777ebfd7ffb96bdf898caee38113c3e10506a7e)

### 🌐 Translations
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/ab59bc7a113e01ebe5501f767adf323d3f8387ff)
- Updated Malayalam translation using Weblate by @Vachan-here in [commit](https://github.com/spacecowboy/feeder/commit/1221c6f3191d388e972653048c5280677e2441f3)
- Updated Chinese (Simplified) translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/0215e61c589e561de914e3f26c210ee4d7d6c76f)


## [1.10.13] - 2021-02-26

### 🐛 Bug Fixes & Minor Changes
- Update Indonesian translation by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/b59e249ba50f282801c6783a88df53e6723a64b4)
- Removed JCenter by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/64d0fc4ce42f401e9fb885423a39c857c382bbd6)
- Implemented parallel load of images in Reader view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9f191f73b899d185039f63af28476790558df9d7)
- Fixed folder name for japanese metadata by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7fcfcd166354eebde65b6cf94e9f6297fcc2fffa)

### 🌐 Translations
- Updated Malayalam translation using Weblate by @Vachan-here in [commit](https://github.com/spacecowboy/feeder/commit/c71b9210488862d1357498bd27a151f18085a87b)
- Translated using Weblate (Indonesian) by @rezaalmanda in [commit](https://github.com/spacecowboy/feeder/commit/253fdd6db4732a840b0767cdbe601bcb524fe083)
- Translated using Weblate (German) by @daywalk3r666 in [commit](https://github.com/spacecowboy/feeder/commit/017de69c1c73da23172de8659722f3c317dbf39a)
- Translated using Weblate (Portuguese (Brazil)) by @eduardo2580 in [commit](https://github.com/spacecowboy/feeder/commit/7d01b89fb4decfc20870c876efec26baa87a438a)
- Updated Catalan translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/c67896b764c162fbbd346f80bcc5c1c89be8b9e7)
- Updated Japanese translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/a92e14d86484b0bc64faea34f04d1b15c39b82fa)
- Updated Malayalam translation using Weblate by @Vachan-here in [commit](https://github.com/spacecowboy/feeder/commit/9a5a8f2e3102a04b99af5668a3219035d99ed122)
- Translated using Weblate (Japanese) in [commit](https://github.com/spacecowboy/feeder/commit/607a0947cd0dc479dbf3fb9c48fcc322168e0a04)

### ❤️  New Contributors
* @eduardo2580 made their first contribution
* @daywalk3r666 made their first contribution

## [1.10.12] - 2021-02-15

### 🐛 Bug Fixes & Minor Changes
- Updated CI to validate deployment on all builds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ca7b750bf5ebd67607a8895a14b4ce5ca6542b84)
- Fixed fastlane so version doesn't increase unless needed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b93f705feceb773116da178e6b5c110f79859eb6)
- Fixed fastfile by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/83bc698694044d9e694f4b4795a4f0bcd4858d00)
- Only run on tags and master by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8ab751fd6ec557de8cb9bb3a40099d00a0c69b3d)
- Removed translated titles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/474d4d683466b6492c971205fafbd2100a2fba8e)
- Removed translated changelogs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f9ffcce671c750d4ff047226aa74ca32b38d8459)
- Removed empty hebrew translations by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eeee3043eda3b9c8164bfc21925a595a4dd4ae0e)
- Removed duplicate files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a71771caa34d91285319717414743049df0f5e76)
- Removed empty translations by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/365bd45cb38870d30fcc6f3e295c159da15376ec)

### 🌐 Translations
- Updated Esperanto translation using Weblate by @Szafranek13 in [commit](https://github.com/spacecowboy/feeder/commit/7b225d25c500e378f2900904ebfd86dc04871917)
- Updated Bosnian translation using Weblate in [commit](https://github.com/spacecowboy/feeder/commit/07443bf7af361385b3bd936744123e665dacc3d7)
- Updated Indonesian translation using Weblate by @rezaalmanda in [commit](https://github.com/spacecowboy/feeder/commit/b088b92361fea0e5408a85419e097c19de44825a)

### ❤️  New Contributors
* @rezaalmanda made their first contribution

## [1.10.11] - 2021-02-06

### 🐛 Bug Fixes & Minor Changes
- Removed duplicate romanian translation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b41e3aaafabba7d010d763cbb540c83067a37387)
- Fixed images in README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c39b1ef09215b78906a52087ebf56ef46f71548b)
- Added contribution notes in README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f57398507f8f9e7df606b5b1756ac1928e519c84)
- Added Bosnian translation from Crowdin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/36a1180f6f7c4e0bd0d29c6cbef552fdcb94e548)
- Removed copied translations from Crowdin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/901b929e8db6a3a3a5e7c8c346904d181f7650f6)

### 🌐 Translations
- Replaced Crowdin widget with Weblate widget by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9e9c46f55ac528bd619413c21891fda23d2d3ac6)
- Translated (Polish) using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1cdef2d9b83a159f4a4c6b4c4b28101acd7e8b8d)
- Translated using Weblate (Turkish) in [commit](https://github.com/spacecowboy/feeder/commit/eafe6fb851ddbfa98ce51d39d14006dd9b05b6ed)
- Added Hebrew (Israel) translation using Weblate by @weblate in [commit](https://github.com/spacecowboy/feeder/commit/29b2cf473195dcb92e205f09a2982c453d484f3a)
- Added Esperanto translation using Weblate by @Szafranek13 in [commit](https://github.com/spacecowboy/feeder/commit/a084f837965e6f83ae6a1390794797220a290abe)
- Translated using Weblate (Italian) in [commit](https://github.com/spacecowboy/feeder/commit/53b67002471d819b2e50e65fdf59f0fad4c24b1a)
- Updated Russian translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bc00fba391563d2fbb26ee348b036d371beec342)
- Updated Norwegian Bokmål translation using Weblate by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/c64b4a57c21b27eaa0ac1f4c892af28fc16be213)
- Deleted translation using Weblate (Danish) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fe842e948c451b7260ca25371ebc4c5ffa028252)
- Deleted translation using Weblate (Japanese) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dc4c16d569887ae09487619f47ab36e5f6472e73)
- Deleted translation using Weblate (Finnish) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cfffe5ed9cf0379de90f54ba616f89511fa2123f)
- Revert "Deleted translation using Weblate (Danish)" by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3b7bfdec126843ccadb2739e0c6c000411a26d28)
- Revert "Deleted translation using Weblate (Japanese)" by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a18c0651ed9d3f1d3f3b1a106635dde669140048)
- Revert "Deleted translation using Weblate (Finnish)" by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6d572878b9530b81c84bf7a525ae7ca819c4d6fc)
- Updated Norwegian Bokmål translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/80eca008c1b1ebec60af24976935cd57ea347efe)
- Translated using Weblate (Catalan) in [commit](https://github.com/spacecowboy/feeder/commit/65b7bc99a05bc12bbfb74e88208f8041fdb9ec4e)
- Translated using Weblate (Norwegian Bokmål) by @comradekingu in [commit](https://github.com/spacecowboy/feeder/commit/853b7f0ae4f44581efad0c153058e31a3e4531e3)
- Translated using Weblate (Finnish) in [commit](https://github.com/spacecowboy/feeder/commit/78112ea3a77929f697d5c63e5169cbda1fb3c03b)
- Translated using Weblate (Greek) in [commit](https://github.com/spacecowboy/feeder/commit/f317a3eeda07f8d11c460e1d1e6759796e02c219)
- Translated using Weblate (Polish) by @WaldiSt in [commit](https://github.com/spacecowboy/feeder/commit/d3a99c111f37a3d073e9b5f644b7946b41c729a9)
- Translated using Weblate (Spanish) by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/18a10d55d298d4f33901d857846d01c811291cbe)
- Translated using Weblate (Finnish) in [commit](https://github.com/spacecowboy/feeder/commit/6ccbdaa38536bf664d3e22dbc8449f9c8b0e46a6)
- Updated Malayalam translation using Weblate by @Vachan-here in [commit](https://github.com/spacecowboy/feeder/commit/0abb909638d8e6bf060290fc44dbf1fc70db61e3)
- Translated using Weblate (Russian) in [commit](https://github.com/spacecowboy/feeder/commit/eb3bbeff8b24cfe59489cf8c50dfe21693dc0fbe)
- Updated Malayalam translation using Weblate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fb557d58d8496843dd000be1687368b6a3594e46)

### ❤️  New Contributors
* @Vachan-here made their first contribution
* @WaldiSt made their first contribution
* @comradekingu made their first contribution
* @Szafranek13 made their first contribution
* @weblate made their first contribution

## [1.10.10] - 2021-01-22

### 🐛 Bug Fixes & Minor Changes
- Updated CI by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a24b4bddacb41d5f8d353e5e24126dae7ba93263)
- New translations from Crowdin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b316df06c2bfd38eb8e64f20ec43c5c8eac4b8e2)


## [1.10.9] - 2021-01-13

### 🐛 Bug Fixes & Minor Changes
- Updated translations from Crowdin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7a715fa4c540d71699928ee7ab2a5e529443f636)
- Added environment variable for Fastlane's benefit by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0c05f58fde0cc0b7e1f3ef52f22fc62786d6fe44)
- Disabled minification due to crash on old Android by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5ac2bc2c14c8bb1711ea34458415061f88e43de2)
- Added comments to some strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0add4d206d579c3656d79ebb80e8a9e2d51dc434)
- New translations from Crowdin updates by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c071f78488861c9f303d505189ede1d9c29662f5)


## [1.10.8] - 2021-01-07

### 🐛 Bug Fixes & Minor Changes
- Fixed path to json file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d862c771a0575c0dae175816c4f692b3e726dff7)
- Fixed reader going blank after opening a web view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/472dc3141305f3491fe1b0a4df5c865863fd1349)


## [1.10.7] - 2021-01-03

### 🐛 Bug Fixes & Minor Changes
- Turkish translation updated by @muhaaliss in [commit](https://github.com/spacecowboy/feeder/commit/45ac6e0963fdae3c2e6a92ee895fec94c76d733d)
- Turkish translate checked aand updated. by @muhaaliss in [commit](https://github.com/spacecowboy/feeder/commit/387db7cdc0664a822452e57c1250b8a154e29775)
- Description in ptbr in [commit](https://github.com/spacecowboy/feeder/commit/6dd67f76771ea43d7f44dfde3fee934ab0d52b5a)
- Full desc. ptbr in [commit](https://github.com/spacecowboy/feeder/commit/3de13944d2c7fe7e95941d2e853353cd4067dcd3)
- Delete .gitkeep in [commit](https://github.com/spacecowboy/feeder/commit/4044ee4849397163bdd6605cbf16a1015aaf2749)
- Updated strings ptbr in [commit](https://github.com/spacecowboy/feeder/commit/589ac04455658afc9b4726fa2d174871efb89856)
- Added crowdin config by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/79c2f826494c36c86552ed05caa559a9c12f75ea)
- Updated crowdin config by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ee6e2a1a36eb5c1bd1e7a45fd7021fcac76d1593)
- Added Crowdin badge to README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4502f535887ffef74033ebc717176ddc3830132d)
- Updated crowdin commit message by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/860b8428c2cbf6fb937ec21100abc80b733b59ec)
- Initial commit from Crowdin's service branch by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a1796949eae059b26d33c78b69c611a454dcadae)
- Removed unused strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/efefa8b3ec13a9e1203211fb78beea6dc2324fc3)
- Updated crowdin config file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/95f335f0cc28da98881114da3a24d58aa890a6cf)
- Updated crowdin configuration by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dd4e31c30e2d5f12b79599e888a19a4319a2e5e1)
- Fixed character in crowdin config by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/10a7ceb278074ddae37c76a067c133f0b2aa867f)
- Update Crowdin configuration file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/94846be81be407cbe362deec1c4499a5cd970c5c)
- Try adding export options to crowdin config by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ad9e75de9b3497518989ce01009d6c8b5091a2b3)
- Does slashes make a difference? by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/506aeb3010070e0448e18498156065a15697a8e5)
- Updated translations by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/27cd9114f286a52f8a76438bdcf67b735917f0d8)
- Configured fastlane by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9afe1789ed857012da13a6c7dbdbc4822bd206ed)
- Simplified translation coding by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2e5b2946b45a99ffa760752a4055a0ff8212c03a)
- Committed fastlane files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a326665a5e495689cc67830fc49561199734e2b6)
- Enabled minification for play and release builds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5fa9116f2685800d48436b36bb6f0bc45345c6e2)
- Updated icon to new play guidelines by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/18492adfc2f8b3e7df048c315d5de82357d8d398)
- Fastlane metadata verified against play store by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/284551b13ba7c87e7ed3de7116f38986483163ba)
- Fixed length of changelogs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9c086bb7b0ee2626c3918329920131743a6830fc)
- Uncommented fastlane steps again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/65f6a65e42ffcaf34b1419322a88251f3f486654)
- Use fastlane instead of triple play to publish to play store by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c8516dc775a0a8826c545bd54f1a18e4e1dd5d58)
- Enabled fastlane publication by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c64f34ce6b5fd895c9a4fe9acdf6ae9f6be34f71)


## [1.10.6] - 2020-12-18

### 🐛 Bug Fixes & Minor Changes
- Added a scrollbar to the Reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/06f7fb81f0fea3030c2dd6cd6e7bdf93f2d6a892)
- Added test for geekpark feed to investigate #318 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/21578a4480ade1bcfe14170a776b531610dea52f)
- Added missing test decoration by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6674e2c5708a16c4d303e56cf9cabd6e01e85e91)
- Fixed atom feed html content being unescaped twice by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/050783895f08949852e525e61dd7d18fa814bb5b)
- Fixed some additional html escaping cases by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e8c5470dd7ab45941ec3015a5e543311a18869ba)


## [1.10.5] - 2020-12-14

### 🐛 Bug Fixes & Minor Changes
- Changed all errors logs to actually include the stack traces by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7b10001cd94aae1837ef77e17cfa5b2bc33cf31d)
- Added a minified debug flavor and added R8 rules that need verification by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a622d65530861eb9117e0bc3a8dae046eaf9eeac)
- Added an additional error log to show what feed failed to sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e1ecc24bd7dcc6c98aaf4f8f8235501bf203b04e)
- Improved Italian translation and added missing items by @lobisquit in [commit](https://github.com/spacecowboy/feeder/commit/487a250cfd625376f818a414cd052d3e71cfb020)
- Adds 'mark above as read' option in [commit](https://github.com/spacecowboy/feeder/commit/08b6aa7f6ff68b4377080211476052228617c8a0)
- Remove code duplication in [commit](https://github.com/spacecowboy/feeder/commit/14c07701b15acacf29a197b0c6122931154dc880)
- Update Spanish strings.xml to add one new string and correct other string by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/6663bdf268bdb968eb0c1c9d62400ed0bca10ce1)
- Fixed scroll position being reset in Reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3ebd53c3c569df3982167ca30a6b784fec8f455f)

### ❤️  New Contributors
* @lobisquit made their first contribution

## [1.10.4] - 2020-11-12

### 🐛 Bug Fixes & Minor Changes
- App is now compiled against Android 11 (SDK-30, R) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/51ef23e6d915c9289477d6d772876ec674ccbc16)


## [1.10.3] - 2020-11-12

### 🐛 Bug Fixes & Minor Changes
- Fixed crash when base64 encoded images were present in feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4827e41cdff90c0bc3f28d85a4c22542d2b6e0d8)


## [1.10.2] - 2020-11-02

### 🐛 Bug Fixes & Minor Changes
- Updated Russian translation by @Fe-Ti in [commit](https://github.com/spacecowboy/feeder/commit/e9787dee7a55d5b25196252f5af35e357ba34319)
- Added compilation of android tests to CI by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7c74100a074caae123a1f447ce8c5f10c06d3591)
- Fixed android test compilation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7de18723206a879fefba7fd347f8dec56d322553)
- Turkish translations added by @muhaaliss in [commit](https://github.com/spacecowboy/feeder/commit/2885b2189aa6330d95a309d932dfff40d1bdee22)

### ❤️  New Contributors
* @muhaaliss made their first contribution
* @Fe-Ti made their first contribution

## [1.10.1] - 2020-10-19

### 🐛 Bug Fixes & Minor Changes
- Update strings.xml for Spanish language by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/756cb10867607a47e5732f48669d376ed3f0da1e)
- Reworded tooltip to reduce confusion by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b9b3bd76e640a3bb9103a76b868596ee63328d1b)
- Update Simplified Chinese translation in [commit](https://github.com/spacecowboy/feeder/commit/19b64a13986deca8c7d2f396345ed130b7c3c45a)
- Updated Portuguese translation in [commit](https://github.com/spacecowboy/feeder/commit/1fd20db4bb592f2c059f12783dd94dd10b0c2aed)
- Increased synchronization speed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a4d8dd3b499085f06e3915c966eb2aafd2301672)


## [1.10.0] - 2020-09-25

### 🐛 Bug Fixes & Minor Changes
- Update Indonesian translation by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/8be9508a93e4b382a427f2398a17ada471764265)
- Update Simplified Chinese translation in [commit](https://github.com/spacecowboy/feeder/commit/751f8665becfe80cbf48cc38af6f5d05b9df5443)
- Ignored test broken by site changing their feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/efeaed6e2fecd4523be5f6bb5a0dbba5db6e07cb)
- Feeder now opts out of sending usage metrics of WebView to Google in [commit](https://github.com/spacecowboy/feeder/commit/2c211b53349168016272a09b3ea5b8306342a616)
- Added support for custom tabs by @emersion in [commit](https://github.com/spacecowboy/feeder/commit/e0a9d261e502ad7b8aeb0b8adccc69d661ddbafe)
- Update Spanish strings.xml for Custom Tab by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/0fdac915a18ce7adcb64b0161cbb81c263852897)
- Added preference for battery optimization by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3a3d3689a31e41d61dea2352a284092ae2dfcff3)
- Fixed custom tab not showing as default option for opening links by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0d17d37479f71413f5101b132f129e22b4310b20)
- Updated version of work manager by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5b845466bdd2699875fd6965a0339f60d9b54d78)
- Added option to preload links in custom tab by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c8b57882efec5d38da713f675f3219b133a08521)
- Update Czech strings.xml for Custom Tab & Battery options in [commit](https://github.com/spacecowboy/feeder/commit/1709c2fdf5bf743cac7b06f3e71928fc2088c04b)

### ❤️  New Contributors
* @emersion made their first contribution

## [1.9.9] - 2020-09-03

### 🐛 Bug Fixes & Minor Changes
- Fixed text formatting not updating with System night mode changes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b4827aaaf649bdc12fcd0a3b32fd5642ed5e4d4c)
- Updated Czech translation in [commit](https://github.com/spacecowboy/feeder/commit/b2cb4a1171cb8f36eb1af8848179578244e72e37)
- Fixed typo in Spanish translation by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/37646e213003230a4cd91fd4ed978213cc0c1078)
- Added Portuguese (Brazil) translation in [commit](https://github.com/spacecowboy/feeder/commit/a2ac933493467012d36d6a751228b317b1155662)


## [1.9.8] - 2020-08-24

### 🐛 Bug Fixes & Minor Changes
- Added an option to disable floating action button. by @mikeyh30 in [commit](https://github.com/spacecowboy/feeder/commit/7d1e419b60760ab8498770b594eda875e17cbfd1)
- Fix typo in strings.xml by @Ratizux in [commit](https://github.com/spacecowboy/feeder/commit/bdedadd5d20c76fe30baa19a74e6aa9b8f44eac3)
- Update strings.xml by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/d8b09639014d13dde04b0f842cf2ccf6c987a7a5)
- Added an MR template by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/000df28c41e5d53186fda9245ca837713a611ca4)
- Added 'mark as unread' to the webview menu by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e3245b9c1c8a6a21b4b1021dfd05d2cec165b7fc)

### ❤️  New Contributors
* @mikeyh30 made their first contribution
* @Ratizux made their first contribution

## [1.9.7] - 2020-07-20

### 🐛 Bug Fixes & Minor Changes
- Update strings.xml in [commit](https://github.com/spacecowboy/feeder/commit/251ffe8d36cadac4f7980ec7e5ac4fc4a5289bc9)
- Update Simplified Chinese Translation in [commit](https://github.com/spacecowboy/feeder/commit/a7b65165120d14d87634511b419fc6dfd8c775db)


## [1.9.6] - 2020-07-06

### 🐛 Bug Fixes & Minor Changes
- Update Simplified Chinese translation in [commit](https://github.com/spacecowboy/feeder/commit/f9fdb071c6af3f70a9ab57c74fadbe156d1a2eed)
- Add reverse sort option to settings menu in [commit](https://github.com/spacecowboy/feeder/commit/d130f0d098914c12571f83a72258eb3f30ead403)
- Add sorting option utilities to PrefUtils.kt in [commit](https://github.com/spacecowboy/feeder/commit/cf70edfe2fed20b387230e9acca08b79029f13b5)
- Modify FeedItemDao and FeedItemsViewModel to allow listing feeds in reverse order in [commit](https://github.com/spacecowboy/feeder/commit/61128e4f604c64eaa06290dbb8728defcfe22726)
- Fix current feed order not changing when sorting setting changed in [commit](https://github.com/spacecowboy/feeder/commit/6f00288310e005d218d6c056621a9e87581e631b)
- Apply 18 suggestion(s) to 4 file(s) in [commit](https://github.com/spacecowboy/feeder/commit/0b3c3a1c817b1cce6d4bfdf805398cf9e046769d)
- Update Spanish strings.xml for new sort options by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/8aa6649e5d565101c685b8e4e7850e308392e276)
- Update strings.xml for new sort options. Dropped string in previous commit. by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/2b9180c39d3a2e3b252f4bee0360394b97e0bfcf)
- Fixed possible crash in case you pressed two feed items at once by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/47bc0a5c4c444ae92a2b44739d73444166a1ba87)
- Update Indonesian translation by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/4b67ce489576be064cfd9f87b0968ce075bb881a)
- Modify FeedItemsViewModel to use LiveData for sorting preference in [commit](https://github.com/spacecowboy/feeder/commit/fd7570e7f909ccea132abb7c478e1f3a08c66273)
- Ignored youtube test which always fails on CI due to rate limiting or something by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4c57959d9bd03ed8c1c524931b4590ea0d22e558)


## [1.9.5] - 2020-05-27

### 🐛 Bug Fixes & Minor Changes
- Decode encoded credentials before request by @philipphutterer in [commit](https://github.com/spacecowboy/feeder/commit/9222bf71685a4073698f0a5f0757eeff2c3d3f2b)
- Update Indonesian translation by @zmni in [commit](https://github.com/spacecowboy/feeder/commit/600071257430fe4fb006bbeb601394abdc4cdb33)

### ❤️  New Contributors
* @zmni made their first contribution
* @philipphutterer made their first contribution

## [1.9.4] - 2020-04-22

### 🐛 Bug Fixes & Minor Changes
- Fixed monospacing of pre-tags by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6003e84cb76178c5089879c637e4d5bb8472d125)
- Added diskuse test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/772553746671bce7bb030c25ce79c6929f7922c7)
- Simplify RomeExtension by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4f7291bc59a3b5ba5528e35496e2effd4edfab19)
- Removed html formatting from titles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fd87f04bd5254b551b716c4b9ee06d29b43a188b)
- Fixed some lint issues by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8dfe42b213fef3e443e6ec9890235e8c848c0384)
- Ignored failing test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cf5695e618f1bc4fce7cde4132c147be1f2f95c5)
- Added share option for feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6f6ed5cad65b3b908ef481f4394a57af50ffb1ae)
- Hid share feed button if tag is open by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4dba37890592b503426088233f736eb82a1a15a0)
- Fixed parsing badly chunked responses by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bfa1a29303a306822977bb5d1463964bb88b6526)


## [1.9.3] - 2020-03-31

### 🐛 Bug Fixes & Minor Changes
- Updated Czech translation in [commit](https://github.com/spacecowboy/feeder/commit/ada5da81e1e740405102a3f0632472693d3713cd)
- Fixed items with no links showing "show in browser" buttons by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c59b2f5c595448cf76cb4cdbf0057a002d5add90)
- Fixed sort order to be the minimum of syncing time and publish date by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/614597d3704488d33eb98ac2711cbaf3eaba74fc)
- Updated share menu to use modern chooser by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5596669fd4c3f95e5d936046adcd2108a1fdb1b1)


## [1.9.2] - 2020-03-19

### 🐛 Bug Fixes & Minor Changes
- Fixed incorrect titles shown in delete dialog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d7eeb8953105c9d5ee6d21c7dc30289fa4438283)
- Added option for hiding thumbnails by @sirekanyan in [commit](https://github.com/spacecowboy/feeder/commit/85fc6d1b5247890fd3c281d85475a7b9f06667a9)
- Update strings.xml for thumbnails by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/3a3df994630890466f54f4fb4fc9b00a7ebf1e66)
- Update Polish translation in [commit](https://github.com/spacecowboy/feeder/commit/1d64c7f45d4d7692f4a066b3ad93933e8c9d2f1e)
- Updated proguard rules by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/298baef7d7fbbf1a6d3a755d48c7d34395dfbda7)

### ❤️  New Contributors
* @sirekanyan made their first contribution

## [1.9.1] - 2020-03-07

### 🐛 Bug Fixes & Minor Changes
- Disabled R9 minification by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/72f6d12aba98ee1c65e08b8949b9580976dfc3a4)


## [1.9.0] - 2020-02-27

### 🐛 Bug Fixes & Minor Changes
- Annotations for all! by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ec8c3cdf5dc29ea59b07c4ce9b8597dd49fb575a)
- Updated to gradle 6.1.1 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fbaf6b1ca505ac76a9f7368bab59c87b6b1e8027)
- Limit gitlab cache to gradle cache by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a4b35158d76e33046b57e9d020e06bfbc8a97abd)
- Removed unnecessary workaround for bad Rome packaging by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cfe431302425467a08a46268da0bebb2603259e5)
- Added additional accessibility strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ae1338e6938f10b6422f6df403e2fea4d8666c11)
- Update translations, added strings by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/ff491f13a9091e3bdf7165a0bff340b2a0369f0a)
- Added dialog for deleting multiple feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/25e960257ef15809fa6004e8f2ebc1be00358b33)
- Fixed colors in theme by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/18c12d76cccb4d71bca3e9bb75fb941a91914817)
- Changed sort order to include synchronization time by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/da3089ecff63b1451933db750fca8244834863fd)
- Migrated from jodatime to Java8 Date via ThreeTenABP by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fa50cfb1b4b84a7bf19ee8e8e519f3118d93e2bb)


## [1.8.30] - 2020-02-10

### 🐛 Bug Fixes & Minor Changes
- Modify build file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1b276295707e808d8809e19560c170b69f5775d2)
- Deploy if tag only by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6d82faa3268efab918af39f784f306daa639de2c)
- Removed conscrypt by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7df3dc29aabe088514ff771ba1bb2c979dfdd170)
- Get rid of warning by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6bd8a8dcb8d0b5abebb6d0792644899e8d489035)
- Test build trigger by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c5d75065f60591b6e4c2c7ecb592c70a0de7ce84)
- Only email on failed builds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cceb04948648c6500d3665f7b0e67cfdc1e6156d)
- Added gitlab templates by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3323506fb756269397e85d2e584739b5787b254d)
- Updated versions of libraries used by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e9a6dc366f03c3b5b2f6b77a9a938be307da7dce)
- Use git shortlog for writing changelog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/994a5f4d210012b17b48b7597f9f30ba631d1840)


## [1.8.29] - 2020-01-12

### 🐛 Bug Fixes & Minor Changes
- Replaced day-night theme with manual control over themes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/49e7f761d1e5236fcb5ff0de7cd4d316b3009935)
- Fixed scrollbar ghosting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d54ccbce1d263c5b953a7e69d36579b472052740)
- Added another theme which follows system night mode by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3614f8a8b9173b1b6312044580d0614dc309eadd)
- Commit missing file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f88f5c460ccd489177520ee7538b598181512980)
- Save navigation state and restore it by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4022f62e424461d7a3275362749254dac08ae51c)
- Added a UserAgent to HTTP Requests to fix sites blocking requests by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/de00629fe1d2bce92be77cf220261e28f3bfed55)


## [1.8.28] - 2019-12-13

### 🐛 Bug Fixes & Minor Changes
- Updated to gradle 6 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b297fc3612b0c96863247fbb5affb30ee10c0f86)
- Fixed crash when opening links from notifications by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5ae6bffe27a31c7c17cdc3aa09b630ca482b7908)


## [1.8.27] - 2019-12-12

### 🐛 Bug Fixes & Minor Changes
- Exchanged local broadcasts for coroutine channel by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/684e694d98270cce7971dc3691ded4c3558b3105)
- Removed all references to dao classes in UI code by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/80b03a07ec093d12d13b047ed376f0d0ef82771d)
- Made ViewModels retain livedata by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/04fb935e2cfc7fc39fb8b56e3dc2abeb53eb874f)
- Fixed some warnings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/af867645ce1baad9627e935a315f0ea2e60727b0)
- Fixed definition of maven central repo in gradle by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d9ff5a441b285851aada38ec2f1d73f9370c236b)


## [1.8.26] - 2019-12-01

### 🐛 Bug Fixes & Minor Changes
- Added parsing of additional MediaRss attributes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/adb4528f2e98701679abc7e57b3030edd7b71fd4)
- Renamed test file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8e4a81f7e141357386929ef211dfa97054a70a4b)
- Updated version of Room and made methods suspending by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7f0ade8a6cb149630f4eeb8e4c3239ef8c498fd2)
- Added dependency on lifecycle-ktx and changed to built-in scopes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/64d73375cc436bd1d59df22e9a6d092b26cbbfab)
- Moved description from DB to separate files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/21173e2993246b7216d6adf785464149d029bcf3)
- Made use of Flow in a place where it made sense by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b85e72b4e3208b881ae2fe0929d9cfc9905b5206)


## [1.8.25] - 2019-11-12

### 🐛 Bug Fixes & Minor Changes
- Added Share option in context menu by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bfb475156ae4bfcee0d5aa1c404e708d298dc97a)
- Added additional intent filters by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1e2366d00130908bf52f7661a158fcd25d3a89b2)


## [1.8.24] - 2019-10-18

### 🐛 Bug Fixes & Minor Changes
- Update Italian (it) translation by @ema-pe in [commit](https://github.com/spacecowboy/feeder/commit/4a520b5218a9b3298fb845d02651c3f56007e432)
- Reverted back to ConstraintLayout alpha5 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1aea42a7edebc880ca6f4811bfd2bc2e7be152f7)

### ❤️  New Contributors
* @ema-pe made their first contribution

## [1.8.23] - 2019-10-10

### 🐛 Bug Fixes & Minor Changes
- Update strings.xml in order to fit a string in menu window by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/49c48ef2d96dcec8cf643356c78535defd19911a)
- Added EphemeralState ViewModel to keep track of list scroll state by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fb22a248769ab553c0eb111ace00bdba2387a72b)


## [1.8.22] - 2019-09-29

### 🐛 Bug Fixes & Minor Changes
- Update Spanish translations by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/0e8fae4a433ee4147bc6b86c1523bd802e9fe1f2)
- Removed unnecessary build step from CI by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/41250990af41e45068d299cd5fc4ca4f415e9d22)


## [1.8.21] - 2019-09-29

### 🐛 Bug Fixes & Minor Changes
- Added build manifest for builds.sr.ht by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3cb9d7e74d09556e93f1154051f6c296404048f4)
- Updated versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8050954fa763c3896b8177d5abb8360c1fbcd467)
- Enabled gradle build cache by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b1c6ccf2b1a2409e3c78c04043e72a7a737e31ec)
- UI components implement scope via delegation and MainScope by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4e8aee56e47fea68b8c4499e3480eaa3ddb84a47)
- Added Kodein by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8b1f6df73584088f9a8e343590a03e269e742ae8)
- Refactored PrefUtils into nicer interface backed by Kodein by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/844c134af45602f121462739383b3c52d437643d)
- Moved ViewModel construction into Kodein by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3a99442b6015a09bfbddb0e8888ed1027a9f5fd6)
- Fixed tests for Prefs refactoring by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/885eb626eb622441081d6a8bc9cd685dcb79f6c0)
- Refactor parsers by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a3b67aa4fdc232751960842a2415452e640f99ec)
- Moved some binding to module by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/39d01a50f9131fb8fc0be51222cc8889bc0e8411)
- Fixed unused parameter warning by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b414bf4f6574ebe0d44b87b801f95849a067b14e)
- Use Kodein as parameter instead of context by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cf79b6a566804f9aa51accbe11b1deda4fc92745)
- Use literal string to make regexes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/83313022883ad454e07a63af00db172431c49401)
- Simplified error handling by utilizing a supervisorscope by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d1d559fe9c524ef5a95162ac8c4909002bab7e3d)
- Use proper typing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5e9ec71c5f68bfa258af2f04b791bd68f7b08206)
- Added android core dep and fixed nullable warnings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/02dd20288d3735f1ac9dfa5a5d528e6d44573310)
- Added v29 styles file for going edge to edge by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cca5aa14baf31524af2cef48b6f3fa83a520a114)
- Added auto scroll to top if new items appear and list is at the top by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3451ee3ccf94bdbb1659594c28fcce487cf0d868)
- Added context menu to feed items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/de3cf6541322a96402478971cd252cf16a2a42ae)
- Added "Mark items below as read" to context menu by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7be0e5e220f1775d2fa2f1060d948fc8e548af96)
- Hide "New based on this" menu option by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9b17aa2080572ca4d256a525e020d5059741765e)


## [1.8.20] - 2019-08-14

### 🐛 Bug Fixes & Minor Changes
- Update strings.xml in [commit](https://github.com/spacecowboy/feeder/commit/e8bd6cf8cdb36758c3090de33f7ab1dfa98f79e6)
- Changed 'Report bug' to open the Gitlab issues page instead of an email by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7852ea84adea55acea056e25a991ea97c3a629e7)


## [1.8.19] - 2019-07-30

### 🐛 Bug Fixes & Minor Changes
- Updated version of rome by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/82f16b10e4a491f1bd67181bb38ab0090f010810)
- Fixed deprecation warning by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3a413182d05cedca80882ff8c9cdd869f3798e05)
- Added option to toggle Javascript in Webview by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4ef6f4d28e81c81a9fe65524a4a4e37df93c8447)
- Added czech translation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5c8c6b74da46fb45a0d51c36a9a216e49a3e3233)


## [1.8.18] - 2019-06-27

### 🐛 Bug Fixes & Minor Changes
- Fixed back button handling in web view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d7fd824a482e56053938b40e5a4362c063f5f5cb)
- Create indonesian translation in [commit](https://github.com/spacecowboy/feeder/commit/d97dbff2c230214a04ec95d4fc1f23efd3e69382)
- Fix Indonesian language code in [commit](https://github.com/spacecowboy/feeder/commit/0b506506a21994345b3aa9228251b7a2a388faec)


## [1.8.17] - 2019-06-16

### 🐛 Bug Fixes & Minor Changes
- Fixed crash when notification contained items to be marked as read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/269b30b725795e14e0559d0e6d3d0b8ad265e174)
- Stop minifying debug build; that prevented ui tests from running by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a24557af90a312460a002c12faa7757c6665d613)
- Made feed title clickable in Reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5c837faea8e387023ead572782c547df9ec67c19)


## [1.8.16] - 2019-06-04

### 🐛 Bug Fixes & Minor Changes
- Removed custom coroutine worker code - use -ktx shipped code by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7fb25be5666ab44e7c9ed514748249058a45adae)
- Enabled R8 minification to avoid maximum reference limit by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6a91500776f40e06a184df94f57df23f99cb8a9e)
- Fixed a null pointer crash if bare <li> tag by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fc2d01e8ead014e15a96b04e322268db874fdc0c)


## [1.8.15] - 2019-04-29

### 🐛 Bug Fixes & Minor Changes
- Improved webview rendering by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/079fd073c3ed17f6577063a4e642855b90b7b4af)
- Removed nested scrolling from web view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0a894a6d042e03519cb48111ca2cedd80fee713e)


## [1.8.14] - 2019-04-24

### 🐛 Bug Fixes & Minor Changes
- Fixed handling of URLs with only user (such as http://user@...) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/723590615d22e9c399ef9029b5cebac08ed38814)
- Fixed crash on tablets and added tablet info to bug report template by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/70525eb7098d32223202ff80843b562aa1cf73fc)
- Removed unused scroll view class by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/45959bfb3e05f986f384a696a99288b8e68abc98)


## [1.8.13] - 2019-04-05

### 🐛 Bug Fixes & Minor Changes
- Removed dead code by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7fdefb9e37fc6de8d0db89567f276483eec48926)
- Fixed thumbnails not showing in Engadget feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1b4c7328a7639284046d5d9292520d278f063945)
- Update some dependency versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3c0ba7dc2067019409fb0d7849bf171585e8bfbe)
- Migrated to single activity using Navigation component by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7e2c0487ba615aee969988d34534e8738e6c692f)
- Fixed webview resetting night mode by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7942506c386e3aad491938dca6953dce26628318)
- Removed redundant Activities by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5776fdf2c994047885668e7bd768bce0c533d2fa)
- Removed debug log statements by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1092b295b8a5d60881ef77e9bfe172972944056a)
- Removed unused resources by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/aca1197aa90a4d9adb19d2f3751a7870af00cc27)
- Fixed incorrect javadoc by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d7fcc870be48ac70c099473bc3c01b7f1c6453e6)
- Fixed spelling error in Spanish by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e5ae5b9a4a15d40a871fe1e244ebd024983c9f92)
- Fixed test compatibility with old sdk by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f0f95c7e26d67e41c53620731ff240a14676d727)
- Fixed edit dialog starting with the wrong theme by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/50c158e6b0e8a0df460ebe2bd2ba7abb6788201c)
- Removed flicker for night theme and reduced overdraw by removing bg by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/78c60ba6025571a6af3f6d55f903eb982a5cc26b)


## [1.8.12] - 2019-03-22

### 🐛 Bug Fixes & Minor Changes
- Add new directory for Spanish (Spain) translate by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/f519719eb7bfbad5cd9c8e02279b17481516554d)
- Upload Spanish translations by @pirujo in [commit](https://github.com/spacecowboy/feeder/commit/b702ce0d7c55caa762041190444faaffc89818f8)
- Fixed webview being obscured by the action bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2261104fa17924a21334ae56d47d75941ffbf3de)
- Fixed sync query test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/545b1b0e16c6e56fc28624e9eaca1c802801d1de)

### ❤️  New Contributors
* @pirujo made their first contribution

## [1.8.11] - 2019-03-14

### 🐛 Bug Fixes & Minor Changes
- Revert "Merge branch 'mark-as-read-when-scroll' into 'master'" by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/00a41d2645babf33df52e15ef6ca788bed992328)
- Removed string for removed functionality by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/10299652bf937e46ea32746b85dae47453ba471b)


## [1.8.10] - 2019-03-10

### 🐛 Bug Fixes & Minor Changes
- Removed flavor in favor of simple buildType by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/51573ceb78c1fa3e413aad44caadea89b9b6bcc2)
- Added option to mark items as read as you scroll (defaults to true) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5ec0489f2b9f3efa79c0c82dad49853ddfb59427)
- Up time limit in test to avoid flaky failure by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1034a989d650a1e40efd0417cae2b39193e5c83f)
- Update Simplified Chinese Translation in [commit](https://github.com/spacecowboy/feeder/commit/70da0ac6044b6d24b5faa06aa9d7c5f5c401554a)


## [1.8.9] - 2019-02-26

### 🐛 Bug Fixes & Minor Changes
- Updated work manager version by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c75fd47b5ca68abbcf5e0609d591670a0cfe112e)
- Stop showing toasts for every URL which fails to load by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f1adc51f5e07e9a277b1584c756d2f23e7b609d1)
- Changed so time of publication (and not just date) is shown in Article by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/820bdaae58e0861b59b12c9e2a7730e4f6ad55d0)
- Increased http timeouts to 30 seconds from 5 seconds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fcbbd72a505a0fd968f349b0d5b6adb12dca3152)


## [1.8.8] - 2019-02-07

### 🐛 Bug Fixes & Minor Changes
- Fixed so feeds with publication dates in them gets some during sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c57e972c54c031a928d9e832c9fea1db839e6843)
- Fixed non-mocked sync tests to be mocked by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5adbd2e405cf38121d113b85b16d67fc2496a911)
- Fixed not being able to parse dates in certain feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c0a497113b20d51e01132a2b746e2c02671f002c)
- Changed plaintext conversion to Stop formatting as markdown by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5009791b8d206f332fa23384c6ba860a7f3463da)


## [1.8.7] - 2019-01-27

### 🐛 Bug Fixes & Minor Changes
- Added support for RTL in the manifest by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/465cd44e434e383420999583a2317349dd1b2f34)
- Added textDirection 'anyRtl' to all TextViews by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/41b751ab4280903b4a86fd39a21c6d7cc0456ce4)
- Improved RTL formatting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fa95fe4ed4b484b4c47522d5b737bbab89399607)
- Marked string constants as not translatable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8257bbb563814d28c5f5de37221d80d87ccba91d)
- Changed so that notification actions do not open the app after by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2b5723f91f8a8356297cd823b9c8e3da06067760)
- Changed plaintext rendering to not include images at all by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8d8f8f6712553eeefe8fccc3ef72b7573aba50ef)
- Fixed youtube previews not showing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b32dc4d54d6c6c5f7583cfea0b8da152f0ac07b3)
- Fixed broken test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9109af4a6f4fcb61c58e4da00f24b8d5b9966b6f)
- Fixed navigation drawer to render correctly in RTL by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/498fdbbfaa470971db20056cd7055752ae69ef90)
- Fixed RTL rendering of feed title and author in reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3d72abb55f70f0c48c4561adaec876fec15a5fba)


## [1.8.6] - 2019-01-19

### 🐛 Bug Fixes & Minor Changes
- Fixed changelog exceeding play store changelog character limit by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fc8aa584014c58c34cf9576578d909eb88502ffe)
- Fixed notification "Open in"-actions not working by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d36708d8b6e1e2cb46474cff48c39e3c6018fe43)
- Added a applicationId suffix to debug version and custom name by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8c9724f83e9b7806fefce98f0450b3aafcb4f5a6)


## [1.8.5] - 2019-01-16

### 🐛 Bug Fixes & Minor Changes
- Fixed parser not handling empty slash comments with spaces by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b044e7e08cca8805f95b78d6eafa124ec32fca85)
- Update Simplified Chinese Translation in [commit](https://github.com/spacecowboy/feeder/commit/4d7a6d214dd84bed9d1d09ba586a7bdf89743d95)
- Fixed feeds with no link not working by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ef01b111332a52d60892f27eb8490c4e43028d5b)
- Fixed other tests by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/421d31eaed48784015e29ed957fc9048d048734a)
- Fixed custom feed title were not being displayed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/418eeda6acb2f32dff29d1553f67d7ae4ae8ef82)
- Increased speed and reliability of UI tests by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2047cebfab3365e04e424c44d972cf5277f33fd0)
- Improved EditFeedTest by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5e32d4616b5c343c369f85953c56c57052437663)
- Changed test to use UTC timezone by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a364a410a34cc2e14fbdc6cfa4b5fe22545caf41)
- Renamed helper functions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e50336c95e17902a349713a47fea9e692ddb8bf2)
- Added test for custom title shown in Reader view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6303767fbb3d32e0bed7f43349cf183db13a2634)
- Fixed timezone issue in sync test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fc0423c633867631741cdcbdc4bac97b233c1747)
- Implemented a test database rule which is now used in all android tests by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/46ea48bd60e1e2ae9ea4fd829a4b1e5048929921)
- Change resolution of screenshots to be compatible with Play store by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/634c107c4963df5dfd0ac21467fae3fbf6e1c463)
- Added play store flavor by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/510383c02687313062d64ec249c33f02eef29889)
- Add play publish script by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/92d2d4a7a2379072d1e47b30970250b24aa52040)
- Made deploy script executable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/55a5a33b3c4be52dcedb2e7740ffec342320e0a2)
- Add signing config by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1aad46d3250490616c279cdf2b3dafd4e006ae96)
- Only add signing config if keystore exists by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9afe09574342ba2887080f3f026289e38615e9e6)
- Fixed track of publication by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/359b2612831d3e1270499b6e00d1981e5044d2b3)
- Fixed language code for release notes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f161488596d810dec470678425d465d6f54906b8)
- Fix language code again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/93e0abfb06841b41005dd0583fbaa5e09ca91e55)
- Fixed action bar overlaying web view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/01a058a3ce6f72a58432be9ec181678279451cac)
- Changed from single task so state is not lost on switch from app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9bece43234be1a5f8467a92bdc5deb0cd80486d8)
- Fixed web view test for android 18 small screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1505b89b0d16443ef6c0debbd14d8eeef962f65f)
- Fixed notifications so that all actions will mark as item as read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/245504d4504fb1a473ccb48c8c9021fcf5c3fecf)
- Added intent-filter for app to show as option to open links with by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1cf408bc6fd32118bcf1ba14be99887f963e63a9)
- Fixed feed results not showing error message on *second* search by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/06931f91b48b38fb2049a7a5be376274ff3cfd83)
- Changed so feed search finds alternate links in body of documents by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/91225fcbcea271e9d1ed30aa497eec412725a8b4)
- Fixed parsing of feeds without unique guids or links (NixOS) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/139706f628e550dec7ccfb9786d595de258bd427)
- Updated dependencies by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1fff8a331cefa1c167e3c0a1ca59fc87602596f2)


## [1.8.4] - 2018-12-14

### 🐛 Bug Fixes & Minor Changes
- Fixed opening in browser from notification not marking as read or dismissing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/12af505e2c88b1aef7110585466cd5ad8451d473)
- Update Simplified Chinese in [commit](https://github.com/spacecowboy/feeder/commit/d103cee8e68de83b88a27ae503e19eb8d40bd1ac)
- Added type converter between long and DateTime by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/47a09174f6b53d711debfa4ce54f6c8eaa9adb32)
- Fixed crash when loading bad iframes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4ab7cbe4f4d90f6f92fbee63ba8fa8d11da249f2)
- Fixed long blog title overlapping date by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2bc23fee3cf5cffd6311eda50bb253ea3b373e67)


## [1.8.3] - 2018-11-28

### 🐛 Bug Fixes & Minor Changes
- Changed to use the thread pool dispatcher for all coroutines by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/73ee37a7b4478f03d256eeedecb990cfaf54052a)
- Fixed theme-specific place holder image for articles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8766419285798ffe27a645c1f8621b0c396fc867)
- Fixed crash when loading bad images by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/52eb811fb2f24dda21b58491f6b57b4f1ecae9d7)
- Removed unnecessary deprecation suppression by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/01f1badf299fe99cfb8bea8c1647e463e4217d0e)
- Add CI step to deploy to playstore by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c635e7f88baa4eecd30abe84ed7eaa0328a3d39e)
- Fixed scrolling position getting reset during sync in Reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3f948829f9f33541aa7fc3b77eee9a1f731aceb7)
- Removed custom layout in favour of constraintlayout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8c9983b25c96def5dc38b3f2cb0058e64f27f740)
- Tweaked colors in themes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5f5225a29d3943f91f3b032f994d4e2f204b252f)
- Fixed a test so it works on smaller screen by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cbd1b54ed25b16c53b8db10e0bb1daeda8e6acd8)


## [1.8.2] - 2018-11-19

### 🐛 Bug Fixes & Minor Changes
- Added menu item for sending a bug report via email by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3c129f9a882619ed93e08fba3d3eec28fea1b7f0)
- Fixed crash when image could not be loaded on pre Lollipop by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/337f7acda94e1b904a1fb22da8e5b0dcf261fab3)


## [1.8.1] - 2018-11-16

### 🐛 Bug Fixes & Minor Changes
- Fixed and added screenshot-urls, to get them back and add the dark ones side-by-side. by @DJCrashdummy in [commit](https://github.com/spacecowboy/feeder/commit/6999024d39563b300147ea53effcba3692892135)
- Update Simplified Chinese in [commit](https://github.com/spacecowboy/feeder/commit/af7825a8e6de735871e47d4960c94db3e5b96dd7)
- Change resolution of screenshots to be compatible with Play store by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2dc89eec4dbe14495384b2a2040b60fca4357c77)
- Update some versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9f1290706451e4838088d80e91f2769804464f77)
- Migrate tests to new JUnit classes in AndroidX by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fe9016843884c981dbc3e4ad248bd83d0709eb9b)
- Updated dependency: conscrypt version by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dedd8a50f0daa5b90d7a9202c7b4073b30e7806b)
- Fixed crash when clearing notifications by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ec59557cf78ac0de5b0f749b0a0d80456c764513)
- Ensure notification actions are performed in the background by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6cb7c548567624907b5ca23eb27f47f13088b8ef)

### ❤️  New Contributors
* @DJCrashdummy made their first contribution

## [1.8.0] - 2018-11-07

### 🐛 Bug Fixes & Minor Changes
- Simplified Chinese Translation in [commit](https://github.com/spacecowboy/feeder/commit/d6f27597fb57b0710c26df8b3b2b8edcc94bd4e7)
- Changed OPML import to set custom title field in [commit](https://github.com/spacecowboy/feeder/commit/680fe00bd474699e365b689eb508f3cecf16352b)
- Fixed type errors in activity intent in [commit](https://github.com/spacecowboy/feeder/commit/b93b96d192a1cf4a32914bcd835ad55a736c05e7)
- Upgraded coroutines version by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7005b98c7390ec658d5ca9377fba5b92f00b0751)
- Fixed ThreadSafe issue with HtmlToPlainTextConverter by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7f39d7697fa4509240df8f2e99cbbf73eb979066)
- Fixed HtmlToPlain test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e978ad75d24b940a161c971f5062976c0670d232)
- Upgraded to Kotlin 1.3 and Coroutines 1.0.0 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/30d5bc4c8ba5996044eedb7f9e3ed563c1a51644)
- Upgraded work manager to latest alpha by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/941caa762a67141623a0156ce04086c2e3866363)
- Fixed manual sync didn't have manual flag by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a9fe01b10a90c3996f6afacbdfe95ba9a8d68638)
- Cleaned up the logging a bit by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0cea83450e9fc5abbad20e203dc9abf6efedbee3)
- Also log when response fails by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/42d39c4139a5f35b479b1ad039260ce01ba27376)
- Implemented coroutine aware scopes for Android by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/874b552939f126831eb514d0ade608000035ccab)
- Removed unused adapter and moved DiffUtil to own file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bbb0b7cf8f9788e03d7a6fea7f3a68908c2344ba)
- Fixed concurrency of sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/58c2119e9669a49c9a439f8f3fe4793bf830d629)
- Removed some unnecessary context shifts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d64f36f65ece5437147fd912e341afe8397f1d3d)
- Only parse feeds with updated content by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/37754d05e9be61df0ebe4242676f15058265e2b1)
- Only sync feeds in parallel if manual sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fed406c9c927f91fc7f0c9928904b8d3ed75a667)
- Check explicitly for 304 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f84a1b39694674a61d9390189f5928759fcb9592)
- Check the hash sum of the response body instead of the header result by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/df07d156b23924ff3100e4cc691edf52afd8df35)
- Reference database migrations in a variable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6d6870439ce5469b5e1790a88c81b3f461c6b6b1)
- Removed AsyncTask in favor of coroutines in EditActivity by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1a03c20ea40c0e5e0f6b57b39b4a3534abf4e32c)
- Catch error in edit activity if site is not valid by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6a4d95b66717c4395870847c27f5cf77fa260f93)
- Catch error if the original input fails by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ccb9af81c46d64bf4747d557267c8006f37911f6)
- Added espresso tests for edit activity by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f60483b827e1fe498fe1b04b6d040eb8112059bd)
- Updated Kotlin version to 1.3.0 and Coroutines version to 1.0.0 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f8c684abd45321feac68ce078a980970a7cc412d)
- Adjusted sync logic by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3aae6c258ef76531d04ba0ba9ca3c7a307ec30c4)
- Sync stale feeds automatically when app is brought to the foreground by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bdacadba17f18d6de2397cec4ec4fa20350bd998)
- Handle HttpErrors better when syncing feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/94960e4713032e1c5e447fe03f886c3aa15027e1)
- Fixed manual sync not ignoring connectivity settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6ab31e31c09ffcfcc5b2c0384182ce28c49a0bb3)
- Changed resume sync to respect connectivity settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0a7a7d84db4ca41ee15740182df9c4c47cf90631)
- Also respect charging setting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/babe57b5dffc0b732b4547671c5034f5535430e0)
- Add setting for syncing on startup by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e2dc604f748e55d67f44cec535cfad54ec2790ed)
- Cleaned up sync function api slightly by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0dfc8d5c98739ebee8e2edd3e69d641123ea8bf4)
- Set periodic flex time to half the total interval for workmanager by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3d7a54994418036a27304257348b783abbca42ad)
- Fixed cache control settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/76e0c2ff3295b8a52f9ff94ce93b7e48e04ece6c)
- Simplify maybe sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a85d77cf4d65916bf140c2f819e206782595bae6)
- Fixed detection and handling of metered networks by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5d66e095a60540ca4461a26d925af17c96445e72)
- Removed option to sync on hotspots by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0ec88f7728a7a97e839859e5ceb675bf2a973ab5)
- Clean up some debug logs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a96ca1ba5a863cbde34381d73125e2067c98a8c5)


## [1.7.1] - 2018-10-19

### 🐛 Bug Fixes & Minor Changes
- Fixed text for show unread toggle by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6e57afa0138f77c2de4b7aa9ca8498a7a2566c57)
- Fixed possible crash when marking all items as read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eb9349ee6fef27ade854c99c499176590a21a3f2)


## [1.7.0] - 2018-10-17

### 🐛 Bug Fixes & Minor Changes
- Added menu action in reader to mark item as unread by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f50e5bfade4553335daff14c5de942b8cf7f1077)
- Changed target version to 28 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/78af617c865004a3b1ae4af0e1153aa04ac4fb09)
- Changed to WorkManger from SyncAdapter by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/27e5793df9455b614823e58f02452d12f09bb8f4)
- Only replace existing policy when something changes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b6d4897d22b274a88f81fd3a022f8f5e2a641eb6)
- When manually refreshing feed(s) then force a network request by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ee0b99242d36a1f1697dca67456f057c336edb29)
- Return failure instead of retry on sync errors by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c54b922a2b8584cc77d2618e157fcc165c88aeaa)
- Add a flex time for synchronization by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7da14a0bc33ed91d516eadcfb54caea1256a2ef5)
- Migrate to AndroidX dependencies by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bc0ab42ea7ba1af18a776994dd2e09c0f62139df)
- Update versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/155a383fc15d3b08ab93d580227f7bc87eccf591)
- Changed header in adapter for bottom item decoration by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5e760f5e651a64a0528225a1b043cae0ec300367)
- Fixed crash for certain feeds with slash comment meta-data by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c442809e108b312a51890d4354742ff14e77c796)
- Fixed deprecation warning by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/222cc8159ca7d85f8738fafabb535f64ed34b176)
- Enabled vector drawables using support library by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/53ca5f13cb9e0536e6fa7e7881c1b0328cbeb18b)
- Fixed size of FAB icon on high density screens with vector graphic by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4ee76b2cf23a02065f4a3a3925e286824442edb7)
- Removed unused resources by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0fa8cbb6c3858e4de7f3936c0ff1f30965ead90b)
- Replaced most icons with vector drawables by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ed3a63d8d0747f16864d1db72dafa9734b6c8b98)
- Added vector graphics for v21 and upwards by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/867370a68dd41e1346035d8be2b30378e6c62a51)
- Removed unnecessary item decoration in favor of bottom padding in [commit](https://github.com/spacecowboy/feeder/commit/4e70a9502974270f75d78126b3c6c9755eec2a38)
- Added a light theme in [commit](https://github.com/spacecowboy/feeder/commit/9d1e9af4d38346246d100eec5b957b240258e166)
- Added setting for theme in [commit](https://github.com/spacecowboy/feeder/commit/4ec0359159aaded47ebec2bab5dfc19d4f8cddc0)
- Migrate to Room and ViewModels by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/529fa9694e9d246936ad30ae18809f6816e3101f)
- Make use of PagedLists by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/22cef13f32b8f36a684078a8419f5cbe76e7825f)
- Fixed flickering due to sync triggering reload of live data by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8c4a5d42022a29604f71dabfe6b805ef268e15a7)
- Only sync feeds on periodic if they are stale by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f21774497009fa6a887467990b12f3a2e60ab2a2)
- Remove unused layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/62b2f1906af4bdebc9e0a85ef1adc8a889ae5484)
- Fixed references to specific dark colors by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9b23e3a28e73a93b4117018bbf6814418a689f87)
- Cleaned up color resources by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/90a91e92161f4fbf49206c375e5a2094c839f06e)
- Changed theme handling to utilize LiveData observation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/17b623f21cb42bea207ed3257d950ede0aecad5f)
- Moved notification toggle to options menu by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/61009dd1c9cf6ebde03e36b4f81728706f4a6869)
- Fixed some residual theming issues by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9e6bbbf805e012efe571dbe75c7ef4cd0dc02e9b)
- Updated screenshots by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3bb9467eb7c6a87f3fba0e9c7e85aa2f0b5cdfe0)


## [1.6.8] - 2018-08-24

### 🐛 Bug Fixes & Minor Changes
- Fix typo in German translation by @Dreamler1433 in [commit](https://github.com/spacecowboy/feeder/commit/883f08c3d55606b52f5dacf5e5af7941239d89f0)
- Update Kotlin and Gradle plugin versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9dd48b6f9bed19974b29c006e607ca5dc42fca3f)
- Fixed crash when supplying bad URL to add feed dialog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/de59fb4957aae10f99da2f61834bfc0363e1a4e4)

### ❤️  New Contributors
* @Dreamler1433 made their first contribution

## [1.6.7] - 2018-06-15

### 🐛 Bug Fixes & Minor Changes
- Fixed extended API to match java nullability by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6a4e5e7b76987e47ae9478179c1eb5a591ddaf1e)
- Updated dependencies by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e67ae0627a00c88217e9ce5c8ec64ff395d1a4a4)
- Updated Gradle version to 4.8 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/327c29c395c24ddfd725b7edcb2ef7da25c62f61)
- German translations updated and added by @chris-je in [commit](https://github.com/spacecowboy/feeder/commit/cd0b73fccbe799ec12137631934ed27d8191ba44)
- Improved network caching by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1e4cf9c438e88ba356fa666e6bea2c4352ae62da)
- Added a second background thread to handle more UI related work by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/caae01aa749a7d48e9d53c8e10571814f86d8f0d)
- Fixed update of views when hitting Mark All As Read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/29252d6f22e527d459fb90a8c178385c18ec09f4)
- Fixed crash on older Android versions when opening a web view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7815262a0c96ddd5a5a6d0558dd5574108a16c4a)
- Fixed test crash by removing debug print by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b014a5da5e4b8ae34747f533d4ca97be644c897e)

### ❤️  New Contributors
* @chris-je made their first contribution

## [1.6.6] - 2018-06-04

### 🐛 Bug Fixes & Minor Changes
- Fixed a crash in Reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ce77f6cb22be765a3489e08298a27ad1789fa815)
- Fixed nullable error by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c96fcdb2aaa4c24644d0bf9c9e492e308595f87c)


## [1.6.5] - 2018-06-01

### 🐛 Bug Fixes & Minor Changes
- Fixed crash for HorribleSubs.info by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f62ee2a10adb100fc1fdcb89165d9502df4b9d49)
- Fixed https compatibility on older versions of Android by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/42fab284716a00d0e4fb1ebc3087893138d98a16)
- Fix compilation issue by upgrading kotlin version by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/00c93534b22d538650ae47436d1247faeebb41d7)
- Update gradle version to 4.7 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9fe799de8b8eb0f5d5145992e495604c47bb0ce2)
- Fixed a dependency deprecation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f15a54393736b84a96f83ba91a9cd576bffaa3a5)
- Added support for username/password in URLs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/651484f191d8ea4fe143f6ca0219959ca72b676e)


## [1.6.4] - 2018-05-21

### 🐛 Bug Fixes & Minor Changes
- Added option for maximum number of items per feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e87ebeea532e70ef170b56096a4a68f61c5badc3)
- Mark app name as un-translatable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/68f6e8425666f5f1d360299fcbb256bde7f4e355)
- Added paging to lists by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5f29ab30a1ec8ba22cdf3ab461059170173e1d36)


## [1.6.3] - 2018-05-10

### 🐛 Bug Fixes & Minor Changes
- Fix bug in back stack by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9f6f357d1c0761c090090fbdad0e4edf3284224b)
- Remember which feed was opened when opened, not selected by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fbc072cc64c305b92cac5cad957760a4d753aae2)
- Open links explicitly in new browser tabs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2efe1f1f99f74b7571edb4d21b924cc4a5844d89)


## [1.6.2] - 2018-04-28

### 🐛 Bug Fixes & Minor Changes
- Block cookies from webview by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e65fd5f907f108483252186674ae61fbccab59ba)


## [1.6.1] - 2018-04-03

### 🐛 Bug Fixes & Minor Changes
- Fixed parsing of some OPML formats by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3a7875569a93dcbbfdd7e1171b033395c3cf4f15)


## [1.6.0] - 2018-03-27

### 🐛 Bug Fixes & Minor Changes
- Remove some hardcoded attributes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/98f0d9471eb37d522d5dccdd2a45dea9cdf98ef4)
- Fix accessibility lint to add focusable to clickable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/703679852ea38fb891e07ad6ecbd6f80d1fa2393)
- Fixed resolution of relative links by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d2a0e540eda1a0d95ca78f77dfcfe8f7c3147c49)
- Remove unused layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/883404a9e5ee87ef40f0ffbe1ef4c4ee34bbefd4)
- More responsive feedback when marking all as read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6511f4fd3b685420b6412aa32296cea3254530fb)
- Some code styling by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5a6bb1b288fba4b993cbe9c84713b758397aa88d)
- Added option to open with WebView and others by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8f3e8fb46470cf168e6e109c2c069aeffdced089)
- Simplify settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0c931f81118621ed3ec100e947f1f80795d838ec)
- Add regular menu items in webview thus allowing enclosure to be opened by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/24d86e90d88d644bc14a2dcec286290176202a34)
- Remove a hardcoded string, and superfluous toasts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/74cb01fc0fb0d7beed47b0c44d66352f1a06673f)
- Add menu option to open link in web view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/77d6cf1114c1cd38d575836d339bcd2d1da0c3f0)
- Make string more understandable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9bd099c8dd24bbedaf6f1734fad0312a2fa18b8e)


## [1.5.0-1] - 2018-03-03

### 🐛 Bug Fixes & Minor Changes
- Remove now unused test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/33182de025898eab2eb284477b72005f135782ac)


## [1.5.0] - 2018-03-02

### 🐛 Bug Fixes & Minor Changes
- Don't render 1-pixel images by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4dd968b00b5ea8bfe2ee6da0b4cb3e1ac9c7409f)
- Also compile android test app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/02ed6ebf36055f9498313a8b93873af040f07de7)
- Shut up about nonsense warnings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/655afe1c8a57478ac6be1baa12c38015b9a12f03)
- Fixed rendering crash with fonts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b2fd926b5814ae5e097eebb8580a2af98ccd6538)
- Fix some warnings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4c351a03b8e9c6817b410ebfd6b7977f18b46566)
- More feeds will now display thumbnail images by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3806d7ddbefd9806c2015cfbfd6b4c6535d0e8aa)
- Make plain text converter into a singleton by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/addd03963e574015ea3a08cad30b8af3feb90f02)
- Fixed HTML encoded titles not showing as plain text by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/27fa3ce3967f0a1f4e72ad014b219e309552f5f2)
- Introduce Kotlin Coroutines by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dc6d059f66070122697da5215ebaac1c51873cf0)
- Fix crash in getting alternate feedlinks by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4b21fb58e2d16f03f166e10f08aedbfaf6bcdb9e)
- Use coroutines instead of helper service by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ecefba255ff3ee956a28be59f4fcc401930e29d5)
- Add panics if content resolver extension method are used on UI thread by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/220cb62b3ffa22d2b50a2290313d257da8000c22)
- Fix database operations done on UI thread by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eac5619063eec5838f170fed4b6ee8d688612910)
- Kotlinize BaseActivity and Deriving activities by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e33b415761eb3e7cfb939b806c17296368a47f67)
- Adhere to Kotlin style guide by keeping kotlin sources with Java sources by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0b2a3aa897e4ec934125eaf7724f4e90e9d6f5c3)
- Add some layout previews by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9a7699509ad0463056bf121105459d23b4bcb30f)
- Improve look when image can't be loaded by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b81ff4f4d250a07a8ef66df22f761d652819d54b)
- Remove long press menu since it's not well supported anyway by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3c5ca3f9454267ba7494a7371b3478a06d2ab5bb)
- Added special handling for finding Youtube feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3e74dc9b2c1e8874b7ae3ffcff7b90ec8f739e77)
- Delete unused layouts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f53cd92bbb83d643914345d086e38cbdabb1e4e3)
- Fixed the notifications by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9e1809d5cfc2e345512e5c7d95209a040b8efc09)
- Fixed a crash by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dee7ce6b5c15a9b9c7b5a0022f61a3b12a664465)
- Add a try-catch to image loader just in case by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/693a5a6dd37e654e76043b1c9fde144d04d1c509)
- Run delete in background by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eb9e146b1537b9f0c8c0b864f0ce12d3a8f45da5)
- Remove old file debug log by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/411e55686f925d81ff86dc0cccfaecf7449488d4)
- Kotlinize AccountService by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a0a695dec99c0650d10014b0d4bdec984f7a9af8)
- Implement a minimal static AccountService by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7e71cbc8b9851c00aa9b71606a6c978ade61a33d)
- Only allow installation on internal storage by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/77794c6a72b99d872885fa699f7c039de6df2a81)
- Allow account removal by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/29307789a93db69d95f440e7d3884a3d886bb0b8)


## [1.4.3] - 2018-02-03

### 🐛 Bug Fixes & Minor Changes
- Handle ENTER press in add feed dialog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8404e9119b57532cb2609cda6487d9282c8d8651)
- Add newline between table columns by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b21ebb4f67dc54ba50f27bd0c7e367c3743998ab)
- Extend SpannableStringBuilder to get a sensible and overridable API by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/acc2c73bddd54d011b25c3d592a6c9fc8cbbfc3e)
- Add test for PRE tag by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f8064b1f57f48b321b46d493a113a79c2cb78b02)
- Improved rendering of <pre> tags by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/63299302753a41c5e1cf7af034c83ee6c8c3a502)
- Some cleanups by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/97efb855e3828045f3328457d05198fbbe20032e)
- Send SDKManager output to file to reduce CI log size by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/de995fefd367892a2bac1c544068db3f4a0ee288)
- Quote variable use by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9f889b4ce3712c766cfdc52935a122c5bafcceb0)
- Streamline SDK installation step by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f9ee32d51314f9f2a9ed84069efd8930727a1b4a)
- Cache gradle files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5c325083cd58ad65e5739274c91b3b7d1b68bc69)
- Cache androidhome as well by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b77f7eaca7eed17fc617e7281418ae4f5e98657f)
- Don't cache android home after all by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/37b5fdad995c59bc2adb81aa9f444177b0f8d822)
- Fixed existing tag not being present in EditFeed dialog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/78531cdd0ca581b4e8b5d72b2e70e7a0b045ba5c)
- Always run Feed loader to catch user edits of Feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/795ae8737cb054808dcecc7463f425fc0937ebd9)
- Stop destroying loaders by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d61dfc6c9234456b41726b5a93b373e56ccfe0f1)
- Throttle all database loaders by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/20b9105a79615b16216f8dcc17597c753ecc6610)
- Make loaders a little faster by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eb514da1db9fc2fb848e8cf3aa8e38bddef85009)
- Remove strange text in XML by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2191b565d1efe2c3f6a89a9cddb0d5e87eb19a25)
- Fix deprecation warnings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/637eb4c89040c3284b65cbe65769e5ad6f336a8d)
- Fix type warning by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c15fa67142fd70163ab0854be6f2434b88049beb)
- Add nullable annotations to Video class by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/427b8925d5e4f680f7b61fadd63e095d3f373e84)
- Fixed crash for missing video urls by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ea81751c181c1ccdd4893f373e4e328db705e125)
- Suppress unchecked cast warning by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/846c2cfa5a27b917bcbb62580507e6162d1265e9)


## [1.4.2] - 2018-01-06

### 🐛 Bug Fixes & Minor Changes
- Add linebreaks to generated changelog entries by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/454f069eb1d6025abd1960d4897ebdc11ed1ad46)
- Update changelog formatting by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6478feceba6a3f548c07d788d895f380eefef206)
- Mention contributors in changelog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0f07701ef25481d306c1dd1a7048e574bfb815ab)
- Generate changelog with authors by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f346d4e78a07a289ccd320df455161cc1e1bca68)
- Kotlinize spanned converter by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d29c57807d3ccee0f232aef5c17d0679bb465baa)
- Don't render script tags by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d2677354f93125e8da6117ac5fa581864c3297b8)
- Target and compile against Android27 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/def896fadc53b667d1d7d1e7df080cc34ceeca0c)


## [1.4.1] - 2017-12-28

### 🐛 Bug Fixes & Minor Changes
- Created Polish translation by @gszy in [commit](https://github.com/spacecowboy/feeder/commit/03fc03012496f76fc3dadb7d3d555a2323fd2d79)
- Remove unused strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0152be9781874b92aa707042c7074fff5bf6db2c)
- Move string array to separate file; reference individual strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1d0412bbe1d5762c8c1cfecaf9b540e37678676d)
- Add french translation in [commit](https://github.com/spacecowboy/feeder/commit/33054661193c4d341620280c9c4a99efeaf935c1)
- Remove translations of missing strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e1924852db13e87b2e6e2babd96239e4a7525bec)
- Add StringEscaping as a lint error by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0d9895bf6244e6601d7f89cf12cb8ea7dfc0513d)
- ... to ellipsis by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/846a6f485b19c5901f25e25dc2a9aca4abe9d192)
- Better formatting of changelog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4e6b49859d18763c6849211cbdd49902960cd766)

### ❤️  New Contributors
* @gszy made their first contribution

## [1.4.0] - 2017-12-15

### 🐛 Bug Fixes & Minor Changes
- A simple JSONFeed parser implementation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/83089f98ba36c4ffdc571e8ba81069ee7c4258f9)
- Add support for JSONFeeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0913f7285c3e903b2e400aab10f529b97db34175)
- Fix nullpointer error when cache directory doesn't exist by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ad14237a1880f79108939c80e2242a86f669db41)
- Fix saving and parsing of JSON feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/948001a36af1c0305af5da1cca17dd4381b26a75)
- Handle error when no suitable activity can open a link by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cffebf7d34ba372c2c1145420c46335dcb328481)
- Mention JSONFeeds in readme and description by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dc9d49b70f9fc3abd6cd250eb494c72c0b56711a)
- Atom type is 'text' and not 'text/plain' by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/86e97954a2eff4d0c84a504ead8d3ab8ffe2f7f6)
- New notification icon by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/359c67ab66047847ee98f0222596fa24f032b0ad)
- New app icon by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ae1eb636430aac2aec27ecccc6b7ed9f16ff6b96)
- Dynamic app icon by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/72c08af2e313ef47e37e81087f2f5dd335ff8f73)
- Promo, Feature graphics by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f4bb44e80115ceff0063b94ab468beb5798c259a)
- Raw graphics resources by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6d49563c93624762ae86b049c7a09b387492a647)
- Run all png images through zopfli - reducing size everywhere by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5779b00365b7b4def9baa9cfa18232268770fa3b)
- Prefer round icon over square always by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5f91e6e56b8cac0922f3bcee599a65db5b1ead7c)
- Added option to sync once per day by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2f7cc6a51b5a8912a642a5d2956f77664d4d9909)
- Edit dialog now opens the feed on save by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/884f8135ebdabaa604b16abc64e13464a5bd4ec8)
- Added app shortcuts for the latest 3 feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e1ec3a4e40fe1492734658a0d80a58d1154aee85)
- Update description to use HTML since F-Droid doesn't parse it by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d9b5d1fbbdef186e5ca90cd3abf9e8e77961fbb0)
- Some kotlinization by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7ff46d41f74d69cc0ac035f6d745f0517200613c)
- Handle relative cover images during db save by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1b8c66b6c5b04196ca259c4b5d79bac76b873d8a)
- Resolve relative image links against feed url by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/924cb3ec5305b3f05a7045fbd1d9ced1d0842803)
- Stop reloading text when switching back to the app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a13a23aad27ed04e9a6d79d277d4f357b49097c2)
- Parallelize sync using RXJava by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0eda56a764d57b4f918b820bc7cce65686e9b5d5)
- Find and display all feed links for EditFeed dialog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4da9e7c2d39166b7d2e48eab0f227b525a4dc72c)
- Align text in editfeed layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fa95e862c8939b77924e644a992f533709ad1a4b)
- Try capturing exception in log by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dcf7dc11a587b4430d6294c47213cceac4cc5e0f)
- Resolve relative links in texts to absolute links by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5b1db2097565001fd6c6aa312913fa7276653901)
- Some code style improvements by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/05aa025233481ae9d5e55bfbe3b3f3d9bf4b3cbe)
- Restrict sync to computation threads by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/52e2fb2400e08dd3e626af33635305aab5a7c762)
- Be explicit about what stuff is used for app shortcut by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1da970567bf1c4bf8971dee95a8fef8cb5f669ed)
- Have feed urls be actual URLs instead of strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f2677f012a4d3a14a1278fb7bb2bc1a0c92f015f)
- When no specified image for article try to find an image html tag by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/712f130aaec084e1e3887a3fac47bacd36573b6b)
- Sync on IO and Computation threads by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c956a9dc4307bba598a1930bc18cd6e6c300b6ea)
- Add feed icon database field by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6e72e2aafe499aac96fea589842ec6021cf84d38)
- Update UI during sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/39e286dd72a290d281fda904b3b73f563d0c905c)
- Reduce duplication by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/420e6ce2c5ceef80416d5486e56191d129ee7c02)
- Use some constraint layouts in lists by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/53fa3f5c9f6a648b996cda7f7e35b0e0020c137b)
- Save feed icon to database by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bd034f292d34cfeaf06027cd64420dcf7e9a13ac)
- Change image to plaintext conversion by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ab4d644c14f980ffff68606039184366eaa506fd)
- Fix type of error to catch by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d482a513f42da01e1aed697e3ec826765a059d18)
- Make sync single threaded again to see if that avoids crash by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c29822ac57d9f0df07e189e3e404fccc7b9f942b)
- Use correct encoding when parsing feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/78d9d2312f37578817bc46eb5594f9e25c286c6a)
- Notify for each feed during single thread sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cbf0d8a53e4dfbf278b047e686c68bb1a7fc9458)
- Fix parsing of Atom page link by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8eff5b945da6ba5dc86126ca0ee49732c2eda764)
- Ignore case when sorting feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c4daae838e96fb6e303e0b73fb7635710812eaff)


## [1.3.15] - 2017-11-16

### 🐛 Bug Fixes & Minor Changes
- Update README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/871362c2df519185e6c151377ee682d575deeeaf)
- Update to build tools 27.0.1 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e6ce8d7b13ceeb026ed8d0342fc833b0c3f79282)


## [1.3.14] - 2017-11-14

### 🐛 Bug Fixes & Minor Changes
- Implement Feed list in drawer layout using DiffUtil and simple list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/901fe3aa1a74bb9355f8946c1e5325d8dcb3579f)


## [1.3.13] - 2017-10-29

### 🐛 Bug Fixes & Minor Changes
- Kotlinize EditFeedActivity by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1191ee79084a7838079be7e22a8b1b9489714476)
- Update gradle wrapper to 4.2.1 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5df26710e976f33b67331238b29e795c492e7b40)
- Target Android 26 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d4d4a572836f429dff302ab3e349a7dfae9b13de)
- Kotlinize FeedParserLoader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a4ccff838b94b324115c4d321fe029db42376b59)
- Kotlinize LoaderResult by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2c4628602050463c75ac9128beacf3fc86670429)
- Kotlinize FeedParser by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7a99919940a98213dfb7ace84922e164be8c0ffd)
- Add new feed now finds feed links in web pages by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f54dc66a4ffb96a9d3b043c1212828e100b5eb3a)
- Update okhttp version and handle nullable case by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cb6b5aa47c7351ef61d06f9e7281958e7aaa7182)
- Fix setting of feed title on initial edit and displaying in title by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7ea9aebe071d619873f5efbc7fcfe10d333d28f3)
- Show All Feeds when current feed is deleted instead of first feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5812c812731cb111a3c07671142ac8770128e1fe)
- Add metadata to repo for F-Droid's benefit by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f2e1327b80e71330f99e12a3bec4dd4212cc8433)
- Update changelog and version handling in release script by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ab55e14f5efc97243898872ac7251c2ca8b1ef25)


## [1.3.12] - 2017-10-07

### 🐛 Bug Fixes & Minor Changes
- Changed so that an empty feed can be dragged to be refreshed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9a92fea2a02fc909ce6cfd12be6993765b888036)
- Update gradle wrapper to 4.2.1 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5ca35bb87e5ff7d6c50bde2df7cc5f04ae5dc0e7)
- Disabled device tests in pipeline by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cdfbf846e30ba4542d0ac6875876084341915a9c)
- Bump version to 1.3.12 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4f8f49dd49605792570dc1a4a34157bdfe0d8b1a)


## [1.3.11] - 2017-09-10

### 🐛 Bug Fixes & Minor Changes
- Added Italian traslation by @marcoM32 in [#32](https://github.com/spacecowboy/feeder/pull/32) 
- Fixed italian xml syntax by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/40a5a673a5e19927285d0e6901a9b3ff3b7d42e5)
- Fixed italian ... characters by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/60c0ce55238dc10e2c10468986324fc0365a1ba1)

### ❤️  New Contributors
* @marcoM32 made their first contribution in [#32](https://github.com/spacecowboy/feeder/pull/32)

## [1.3.10] - 2017-09-03

### 🐛 Bug Fixes & Minor Changes
- Fixed crash when toggling 'Notify for new items' on All Feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/de88c05398006a90d536b322a52092a1722615fd)
- Releasing 1.3.10 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a27409b44ebe38f77d302b00873f34623a5536b2)


## [1.3.9] - 2017-08-11

### 🐛 Bug Fixes & Minor Changes
- Fix notify icon on small screens by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2fafc30732ed5bf44e9c4a8023082641a4ec438b)


## [1.3.8] - 2017-08-03

### 🐛 Bug Fixes & Minor Changes
- Generalize CI file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/530d81d21ad26b5fc9caee39eb17040b3cc51081)
- Enforce Kotlin stdlib 1.0 to avoid platform dependent methods by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a834f3aa3524e8ec17b36c87a7b77e853b22d76b)
- Remove references to Kotlin 1.1 StdLib by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dfe562453bdbb4de32af3e6fefbf7f98adc2d55a)
- Add emulator tests to gitlab ci by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1022ff667c6769339be4edea339ac2412e65210b)
- Bump version 1.3.8 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1a872117409bd10f8fd638365ceddca343d1e07f)


## [1.3.7] - 2017-07-27

### 🐛 Bug Fixes & Minor Changes
- Add a show all option in the sidebar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1a12cac726c2111f35597da6b4fdabd30e298329)
- Mark all as read action in show all by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/01dcdb6de4627c70e146ed8f436bd97c963440ff)


## [1.3.6] - 2017-07-21

### 🐛 Bug Fixes & Minor Changes
- Fix crash when parsing filename from magnet link by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b4fe58540ab165d3b52866181ead06441c8b6861)
- Fix null comparison by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0e0346fa154767319892ce33f2f9b31d01e93941)
- More sensible comparison order by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ae7167c154dbaae7c8a6a965818ecfa26722e533)
- Update values-ru/strings.xml by @antonv6 in [#31](https://github.com/spacecowboy/feeder/pull/31) 
- Fix crash on Import/Export OPML on SDK18 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7cff8dccf45140dde65a09ace589af0028caa806)
- Add lint error for InlinedApi by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/62ce2be9a70d93a81a8f82a213e1cb8435efac2c)


## [1.3.5] - 2017-07-08

### 🐛 Bug Fixes & Minor Changes
- New german translations courtesy of @dehnhard by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c201545beb057e3e7d6bf85bf5eaf1bef7f58b31)
- Add missing spaces in german strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3cd3470c0004a7158ffe95fc4b10d4a5d0d3e494)
- Add dependency to constraintlayout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/87fc158ee4c2ed974ec05897a87ca999a2757eac)
- Convert some layouts to constraintlayout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/51ebbc5333aa411d08845af8889127c2bc33688b)
- Remove top padding in feeditem list since app bar no longer overlaps by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1e2f84d78be40f41b8c0bb5ac6d3c43991c62f50)
- Tweak layout constraints by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/42b82a686157fe4d2065480aa4f75dfab1d6ba7e)
- Improve empty view layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3fd3bd55f7c6d3e36139e8ea8c09537924f548cb)
- Reduce size of empty view text to contain german by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/83bcedf64e82ff75de04131e14c661f7279b1009)
- Fix odd scrolling bug by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6412750bcebd0a0dc4439fe2e87563602afc670b)
- Change to ArrayMaps by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e67c8fbc443f9c10d2ee0d4e147ed1b0ffea4ae3)
- Update to latest support library by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fd9140f1d691c3b970a740122e00c42ce822a862)
- Fix location of sync indicator now that list top has been moved by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/439d22a1be5be31fb8500ec99d56d808cdad7e3b)
- Add test to make sure londoner is parsed correctly by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/81d217d62d70126a4e5f1aa0211dad2e9c84faf0)
- Kotlinize FeedItemSQL by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cf71f50db6e363401ab45dbe26f492e2fa415a8d)
- Update to gradle 4.0 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3fd8d33204479ab68b28221baf6f3188610578a2)
- Make sure app doesn't crash if cursor has too much data by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2cf6604107d13afe1460291750808a7712e66bc7)
- Dont load description for items when loading list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/09ac9c58080598fa5357e5246fa0dd975d74c58d)
- Get test running by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/980c746b17d22d1457ef2d6a696d6e7e450a3272)
- Fix OMPLTest by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/14eadfec620398ce582d8d66a6893fea70cc8553)
- Fix OPML writing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ec7523a126ccbcda72ae79b19a4ca376819000b6)
- Fix OPML parsing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a66c905ee5d4431c632de57b8b7200265a121341)
- Fix opml persistence by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1d9160dc33daa4d3c80dbc8f2e952f2e8c83f709)
- Load feed item's rich text description and title in reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/381cb2dda07a20b40febe57684eb179b43853819)
- Update writer test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/405fe739f320478c1cc35e65ceda0ec461663cb3)
- Change to 'Updated feeds' instead of 'New RSS-items' by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c91f111df316f23af84fb922966246392029e296)
- Update version of kotlin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/deaa60682d89ef3cd3a641979a04952a49217d53)
- Use gradle with sources by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9bbdd7a53250fb606a9d4b97aa817a8b10897f5c)
- Handle case when cursor is null by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/962687299724115d4647f2b7c88119da5a6f15e6)
- Move OMPL test to correct package by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ad78b8b80c72ddf9fe1f36c68c9c008e1f5e2d19)
- Add tests for contributed OMPL files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/272082779deadd5b8c4c2fd5bae8de0f32d2c45c)
- Bump version to 1.3.5 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4c6e2954cee437088994a4fe296f6cc9884a4edd)


## [1.3.4] - 2017-06-04

### 🐛 Bug Fixes & Minor Changes
- Use updated date in Atom feeds if no published date by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/08df78d6226806458fbc5b71799da0e0e3319095)
- Timezones can differ by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/864849e150aa1e0fc82624967620b28d0c25e266)
- Add a display title field to feeds and use it for sorting in list by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3307587877c0857d06ece9911cf6ee70a7c071be)
- Switch to ArrayMap for better memory usage by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7afc600b9b3a7501bb88c6f2f223b772bd1fb94c)
- Create values-ru/strings.xml by @antonv6 in [commit](https://github.com/spacecowboy/feeder/commit/53ba05f2c4ae3205f99b661fb0c2cf2e987ac97c)
- Translate sync frequency array by @antonv6 in [#29](https://github.com/spacecowboy/feeder/pull/29) 
- Extract string resources for last settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c2ea1f2544427f8a56bc61eecde3007b66bbff96)
- Remove unused strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5285b7ac4068d5d05893433fc906072e20c14fb7)
- Update values-ru/strings.xml by @antonv6 in [commit](https://github.com/spacecowboy/feeder/commit/b5bff709fda29471901a580d705c90926429d62a)
- Reword tags by @antonv6 in [#30](https://github.com/spacecowboy/feeder/pull/30) 
- Remove translations of dummy strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3b0a7162a9588d07699637d64366135080b84266)

### ❤️  New Contributors
* @antonv6 made their first contribution in [#30](https://github.com/spacecowboy/feeder/pull/30)

## [1.3.3] - 2017-05-19

### 🐛 Bug Fixes & Minor Changes
- Don't render markdown links in plaintext snippets by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a94544604ac509d695ce128bb432ef9367c27c7e)
- Update unit test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5ac6d495b4ce6493b7ffe368d22bbf203f71ff91)
- Don't print so many newlines in preview snippets by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1bc7c0bf1cfabfbca1eb1b981355e34c83dc8bf7)
- Convert non-breaking space to normal space by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c2bffad40b45ce1bc166ffdb19a4894d668d91b3)
- Ignore style tags by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/091478975f9d37631af2acb382ba862983bfab2e)
- Updated de-rDE (German (Germany)) translation by @Roxxor91 in [commit](https://github.com/spacecowboy/feeder/commit/e270c11d76a329d828f622f310faa3d0dba1a2e6)
- Fix german strings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/93d5336a01e81c571b1bae3db3be5e4e2db6d403)
- Catch no such activity exceptions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/02b5aee735241dd2163523d378f3b11588304543)
- Add kotlin test folder by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/29059dc45e0527d26cd6cca0f13e4a30f2afc8be)
- Add test for relative links by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3b30c5cb628e052d9a99bcd965d36fb5fd03cb27)
- Kotlinize FeedSQL by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/210211295a39d180595ebfc2f4edab7ff68523ec)
- Fix database view statement by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ea7e9cd6b7e3fe65d02c307b174e7a713477c51c)
- Don't crash when column doesn't exist by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/12749a102346b70b4de5b87ca1b7cd37a1b632d6)
- Set feed title depending on if custom title is empty by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ff8c8a1dba1a56d69e0659389150518952e6cbaa)
- Had missed setting the tag by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6a1b62489cbbf26c5ec272f66dd1a185468d8d42)
- Fix default fields for tags query by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a8947d5ca2a95e5644a0eeecc007e8876d28196d)
- Forgot to iterate cursor... by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/71324decfa91eaa4d4bed2b3a44bd3ccc23a5eb0)
- Update GUI after each feed is synced. Rome is SLOW by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/60da3eeec54e568487994c62115d04e748a27ce4)
- Fallback to feed author if entry author is empty by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7662785de3f117987a5d91deb7bdd277ee649f39)
- Update feed items if they exist instead of effectively ignoring them by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/78a6cd14383131cbe04e503485b85844114232c2)

### ❤️  New Contributors
* @Roxxor91 made their first contribution

## [1.3.2] - 2017-04-18

### 🐛 Bug Fixes & Minor Changes
- Add missing permission for SDK23 and below by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/292cb21595d7abd31c90d7320efff84a6ce9deff)
- Fail on lint to prevent future errors like this by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fff4f31d4fd86f4ce4b8139a3658bc7c17568f91)
- Ignore a few lint things by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d46aa106fde60c3e7bc075fdade45b1710cc668d)
- Works on JellyBean18 but not 17 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0b422bb33f8b3cd147fe49babd350e829f78db75)
- Enable espresso - migrate existing android test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/00e0b1a41bc97ed4800fb4c6f39b38ee28505e93)


## [1.3.1] - 2017-04-10

### 🐛 Bug Fixes & Minor Changes
- Move extension functions to own files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d19b641e1922062d3818c80c343709bbb3cd962c)
- Move FeedActivity and OPMLWriter to Kotlin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4561c515947c180f9713d7ed369bf0c09de0085c)
- Fix OPMLWriter (and test) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6b57279ef9a7cb348c5417b7a87a3cbd0c994d75)
- Fix conversion bug with fab not marking as read correctly by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/458c4ab46a915b7924be82058acdf83ccc6a1bbd)
- Additional extension functions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3d174081d3bfcf96e42810f3e1b4a4d7eaef68d0)
- WIP moving FeedSQL by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1c3992654edc4c47ef6d2a1ff5e6a756f14e6acf)
- Added gitlab ci file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7b5113611137380b990796bcb6990d1d264b633a)
- Update gitignore by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/29ea0eeae767061e4737ebe8b8d726a530ece5c5)
- Make read story title even more readable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/31482fefcbdbfba3ddc4df06b6b46f09d1300475)
- Make method public by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/02eecffc28fc90f878e88c0711738c5c1e977538)
- Kotlinize FileLog (reduces API requirement here) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/634ca7f0012ecc98e26e08413cb4d252981f3af4)
- Make systemutils friendly for older APIs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b156b62ef47430268186d4166260129822a6b241)
- Delete unused files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/570425c1859f8f0bafcc97c85f7914054366b7c5)
- Make urimatcher static by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/35d74c79d9e07f06dec7cc1c1895676987e24091)
- First feed load needs to move cursor by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7665004e2c95b171b969c8be3a8a82bd2cf2f00d)
- Suppress parceable lint by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/09d1d01939a489588280dd1e8937f6aed09156ad)
- Update android build plugin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cce99b3d4a22a492703263b0bfd8e2c8619f32ce)
- KitKat compatible by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ad8837b0dcd4892157c67a8a188bc1ad65a4b2aa)
- Remove drone file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f66bab8c97a78ea85b16cd2cbc54f8308020020f)
- Add build badge by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b3633df6a198ee716d333b7aad1acb410a3f4b4e)
- Rationalize badges by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d350478b5689e9cece4bb2955315f814c07fe2cc)


## [1.3.0] - 2017-04-07

### 🐛 Bug Fixes & Minor Changes
- Button for f-droid by @Poussinou in [commit](https://github.com/spacecowboy/feeder/commit/7c70a16b5300a212eabbdf40ee0106533c541f92)
- Extract strings to resources by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/83f676b26e6c76b0ad185e8fe4a8be5738250d78)
- Japanese translation by @naofum in [commit](https://github.com/spacecowboy/feeder/commit/115e55031d8d4b3c9c834dd68cbeefd431bc062e)
- Updated by @naofum in [commit](https://github.com/spacecowboy/feeder/commit/8c933f05488673c2928a0d4b2e63c8e448ecb3ed)
- Add settings activity by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b8289f0f259072ca69636021b4dbdc4a2925836e)
- Intent-filter to link app settings from global data settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/835ff6a74dccd4b714dc2c8fc50e732c613693cc)
- Implement sync settings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f9f8ceac28bbd8ab43a8d56718b65dfb9a169dfc)
- Remove outdated image from README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5067dfc829c297db9284b16d3fb388a1f12e1021)
- Reorganize html conversion classes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ee0bc77ecb5f16e6d666fb314c933d8756e367cb)
- Option to only load images on WiFi by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3e2a3bf84960ee7974717802533ddadabcd6c65a)
- Handle possibility of Null due to network changing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f7485a5a6fe4037dbad4710236165a4e5be71fa5)
- Only load image on WifI (if set) otherwise fall back to cache only by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/557943f91fc3ba1dfd4f6caa5e9e7db339e59956)
- Fix image scrolling bug by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/59a97618aab15b00e7940dc29ac55d9255399ac5)
- Use same glide everywhere to make sure cache is used by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7592661f49737a6326fb9bd8f3b711c27ca2b767)
- Nicer indication that image could not be loaded by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d5a744c42de604deafb557dee6771a9d35d84c07)
- Add setting to determine sync frequency by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/553fbf0750e55aa8a62a39139b5ca98281ccf277)
- Enable kotlin in project by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/611e121c6d6b89056d63f73f61f9151ca9c4a096)
- ContentProvider in Kotlin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f26da5050f6cd19fdbcca9884c61e84629a890e3)
- Fix a visibility error by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a6eda32e269b4f4a2a0d6bcd635baa495ddac6e6)
- Migrated static stuff in content provider by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f932371f88334d7cfce536255cef87c7cd2848f6)
- Entire contentprovider ported to Kotlin by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/61ef063b41ef0e99cf79dfa9b6ec0735e728c100)

### ❤️  New Contributors
* @naofum made their first contribution
* @Poussinou made their first contribution

## [1.2.3] - 2017-03-22

### 🐛 Bug Fixes & Minor Changes
- Fix some lint issues by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6ba05e508d00b869e13e64d708c28980b5d3997a)
- Get rid of Java8 Syntax by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/aa21f9136b96aa047ce20511a0f11e22030e8c70)
- Revert "Fix some lint issues" by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0460d74724734395f2154a42bb170d0c3dc17b02)
- Fix Rome dependency and remove submodule by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/31801c1027d38d04ab03c9b5440be09dc130d36d)
- Restore compatibilility with Android21 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ee424ad9d462632c02ff1216b5093c2eae42fa4d)
- Naive table handling - at least make sure rows are respected by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/02e445a5ccbcc67c878bd2e698015b41665df86d)


## [1.2.2] - 2017-03-12

### 🐛 Bug Fixes & Minor Changes
- Update license to GPL3 for Apache 2 compatibility by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/97f130a8797ac32d4b16d0d5c2da64729b692395)
- Remove tagsoup jar file and use normal gradle dep by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/92cc270798bf20eeab2a6670b6c9959ab5ed539b)
- Build rome submodule as dependency by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/07848c632d60a20bc74f5b30f251a0d9d718bf18)
- Update README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7766b053a264ffcb408d1bcc31420a48267ab3e6)


## [1.2.1] - 2017-03-09

### 🐛 Bug Fixes & Minor Changes
- Add rome with gradle build as submodule by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/58ece0d756fd1b27a8c9ceda20395013ea5a6b23)
- Update gradle to 3.4.1 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/74eecef4349516d6255a6d7c9abc4cfcaa1940f0)
- Fix OPMLWriter test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5707f514feb34b0cf8153fe25c9a28637dfa87e1)
- Use YahooMedia extension to load thumbnails by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4f17facd05251d7990991e80d1dca1ebc13d34da)
- Update submodule by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6a04d8ab924809208f393cc9521d9da51c9d812f)
- Use only the jar output of rome-modules by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/15169e84ed28a4a32e6bd80ea3ebc286781e18f6)
- Offline test by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6f69e0831e3628345d6846f328e41c43158785d7)
- Fix should be feed title in feedTitle... by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/168c4174ea9cff9f90bd0b1ef387be35ff8e4655)
- Fallback to defined media entries if no thumbnail by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8b5d5955f149645c00f292422cfe305fb3de0f4d)
- Add ignored test to find image in fz by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3c7b4af41e99061bb511f13401d506372b6cfed7)
- Make URI notifications a manual action to avoid spamming the database by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3f086e2ecd5185ab9c2191f2d6d20960d9585076)
- Missing notify by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8503222c03f5d110d931df8851d23602d5e4b4b8)
- Add missing notifies by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/50111aa87506d9b4d092c1bd454713e95426a275)
- Notify on ALL uris by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/18947cd0167b291356d15a05d7b01d1ab99636e0)
- Only network is parallellized by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c910f6e1c52d6aef1c0d4be703620fc57aa8a3ce)
- Make double sure no parallel sync is done by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1b77585a3eef1b928a91b21582e023c009b372c0)
- Remove unnecessary permission by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/41311c4f89c5e22d171ac92c1ffd54cc1c1f63e5)
- Try and understand exception message by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0b16c13fe599f6e0da1f6a3363b8039eb4565dd0)
- Bump version by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/636e39e37365f7bfd1688220e95f20ca7b56a439)


## [1.2.0] - 2017-03-05

### 🐛 Bug Fixes & Minor Changes
- Add a debug log with viewer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1f02a719013f1514e40f53240139f2ee2c89d7cd)
- Fix spelling mistake in service... by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6b6f5b1c500b074cbd2a719d0c85ba5faecb8e78)
- Update SDK and Java versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7b07c6a2fbac3e92b221ada3213e4e3992f36870)
- Remove feed search by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d0bfa44e8ca6dfd5fc819e6bb93ab29ddebd5600)
- Local RSS sync proof of concept by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/497bf2955827a59d5b3b59f7396c4b1916213568)
- Add Caching to OKHttp by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2edf113e7e6fd148c75a1d62bb7bff129b92127d)
- Finished conversion to local synchronization by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dd14162beeeb2683aba92345ff114fdfea2df586)
- Remove dependency on circular progress bars by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c7a1d97f31f3af69b25412574ad216667e90f4db)
- Remove unnecessary permissions from manifest by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b8047475eeffc91f1b72adcb5ea4f7fbbdeaa6a3)
- Bump version by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2d4c99551f97cadf0b979b12c015716ea22fb67d)


## [1.1.2] - 2017-02-08

### 🐛 Bug Fixes & Minor Changes
- Listen only on localhost by default by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/69e97a928fd9a22a57241f0ce26c3fe4e863fce3)


## [1.1.1] - 2017-02-07

### 🐛 Bug Fixes & Minor Changes
- Add missing dependency by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/001b1e1321c3ec2e640aa58eea1df00b2dfb6559)


## [1.1.0] - 2017-02-07

### 🐛 Bug Fixes & Minor Changes
- Works on kitkat by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/47681655fb946a59d23858c6a5cbe2b686641de8)
- Basic add functionality by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ce493ee09c01696ad9249f998374ffc5018ff2c9)
- Small improvements by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/50ec7b962a66dcb823252393b84ac7b4bb0192b8)
- Fix wrong link index by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b8e51073b31aeb8e19a835f61e753d4a8a139f1d)
- Some animations that WORK by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9d0a9c8e7c78f44ff23d7665a0b0cd1de00d4dac)
- Added exit transition for editfeed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4a8a679bedfcbc6530104f90280068b5f6468eac)
- Exit transition2 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/74413486499b592a36df0b4638dd4f91bce734af)
- Added some menu options by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7bfc76225daaf055e83eb01ebd6b765e38f33511)
- Handle gzip feeds (XKCD) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/09579fc2d07a2ce017393356973c7cbba09ba415)
- Fix gzip problem on CwoboyProgrammer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/798b6227cd1d36d8c835970503ae09ab85a0868c)
- Add xkcd to defaults, tweak view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/99c289bb30ebf9a29571388cedccef5158acd55a)
- Fix some scrolling issues by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a98d6ab88360b356281373bb25ce503d5f96e0b0)
- Fill in existing info in editfeed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/849f695c9f056d4b29caebcee46c9326b5a7c3aa)
- Use better network stuff by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bb8485d8046c7c76cbc7b90954c0f781935b605f)
- Need to parse dates by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/927c330c4e7fcbbe8b5cc279883be00ac53c8c6b)
- Possible null pointer fixed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e53be29f9630e12f7d66e150d016129d67a17c22)
- Handle redirects by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1fd24c5a79b3cd4bab6efd9577e33e3066880efa)
- Generalize feeds a bit by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3acca9ad33dc1bb00f4f40a90ed3c74d980e6913)
- Show date and open empty stuff in browser by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/073303c282c05d573757adb4f8e03935f0ce8b99)
- Display images, first take by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2eb316adf3a284e52f874e788289818bc8dcbfa7)
- Load text before images. Scale images if big. by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5809ebee83a82d7545f87d930c8ddb7a40fa1ca3)
- Use ellipssis by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9ec0cee5a88c3f880d60b77f333f398fda0a581b)
- Handle some more tags by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/16a02c950f67ab139023fe4d9bb276f782cf0c9d)
- Database utils by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5b44e77fd2952dcb51334864f0283c4e12dba8ae)
- Some sql changes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/44e19801bf4af0d78c6304371868a8ee84017b36)
- Add FeedItem to database. Add SyncService by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c7a49d299b680f1c47ac0eaec5929d5e510b0cf4)
- Sync to database, load from database by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/871a39acb8a504236c94660db9d784b0e33753fd)
- Notify Uri on sync complete by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/07c464248aebc6c06b6ea455994d4ab46a21c5cb)
- Now shows if item has been read by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ea95b6cdc7e2ccd7702ed3542ddaedcc25652c44)
- Make text selectable. Links are now clickable by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/445e0f5f5bb142f67d958fe34b4f16d84971e597)
- Method for getting all images by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e9ef3056236149c1fe77984e65235d91a602d20a)
- Added unread count by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/95458db5fc8dbfcde0894c26a4c3fbb3e831446a)
- Improve unread handling by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d91fe3ffa3593cc2965ddf9717f7da4717052359)
- Show only unread/all by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/887e292638181cff780fc35635fe07c93cc4ae63)
- Set a date if none exists by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3cc1328b51ba959b237c8238c68c34203a109c13)
- Show timestamp in local timezone by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/63db915d3d3bd34f20e125ac0ad83a1d0501e492)
- Fix share intent by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fa4d27eee1737e80e6c7c2d9b4e15d3d5e28063b)
- Remove redundant class by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1fd1d772027a74204d931a6c1aa0baf63d2d141f)
- Prefetch the item image by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a8496f96ea1ae59a72a0bea0d0f44a39680dfd6e)
- Expandablelist adapter for tags and unread count by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e206c6622a17e66d74276c9a1c850b5f09ee14f8)
- Added license by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/28176d011853bc24228805b109b9cb32aff84ba5)
- Added readme by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a4d560b58e33374aef98c19aae1e7044447fef09)
- More readme by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a6c266b44ac50eefd7efe1e582284cdf103cae8c)
- Switch to a BaseAdapter, since SimpleTree suxx by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/112124d0961302b51b446a22fae18ffd5414c19f)
- Added proguard configuration. 1.3M vs 943K by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/20c67088799c0b3dfd4a0eeb83da7996e9695668)
- Display all feeds in a tag by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8a511368ad8cab256767f64ebccb79f8e0263030)
- Can now expand/collapse, as well as show entire tag by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ec41f91dbd4b8f8dda28ae5da4486185f1230eb3)
- Scale images with screen density by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5b082adaaa506f578cb0f17694b2a404ac8e2e5d)
- Show description without bloat by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/64bc64b6a32650964abd1a0eeaaddab788d06440)
- Only notify changes once per feed on sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/95cf010c93b6fe99d887d6c6cd87985dd778b94b)
- Remove empty space from descriptions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2e938d376877594b9d479466968e72d3b5c45bc7)
- Save unread preference by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/99a339dbebabeca315f998566da9fc8bcc58b85b)
- Do image resizing and insert alt text by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5beb0b30ac7b7a0510415f49260542a3b0106d28)
- Use JSoup to parse images and rezie by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1763bc0c9c4d35d727deeba4f885a43723940cb4)
- Switched to an app-engine server to parse and cache RSS feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/568362f18fcdfd0b210c0e3f69150f10dd92efd9)
- Parse atom timestamps by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/50d51fe140feef29441b2f5c11565a6cd8993a11)
- Convert timestamps by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4ce22276b1b1c471275091f8b6c3225c0085fe2c)
- Add doctests to cleaner by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d2caabbce7159a8b2fa3666207869c5105d694bb)
- Timezones are not suppported in py2 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f2f06fedfc0b9c7753ac85b24927573b731d8b6b)
- Only sync new items with timestamp by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8835febecbfd57b6d39453c28b8eaa89ae42cbfe)
- Future support for read flags by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/224d630f13fd1c91e7055b0386efe9ee18e780f9)
- Added all existing bloat patterns by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a1e9c5727fd481904dc3f9ac109bb22ea36db81b)
- Also remove empty divs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eda08f8c8de2445981be4c845b3876bec0de18d0)
- Remember which feed was last open by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/894583c902cc77143329a843e8578e0a2bf2d7c5)
- Cron calls GET, which did nothing. Now cache all by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3c015f1135d5bed465884c5545b8cb1023b40fee)
- Remove some prints by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eee7c094cca515943251277f4830bbfa30225774)
- Escape html and reduce number of writes! by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6094dfcd36e91f90d558f2c3d842969bcd865ae5)
- Change multiline to dotall. and add doctests with newlines by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ae4b12b61b852971b4e640318a36117420c7e780)
- Show youtube thumbnails and handle clicks on them (iFrames) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/71c8373ceab0a22ed17db5e490aa08abc060fff2)
- Use content if available instead of description by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a7b8899feb84a769baa5aa577a318cd9329135ae)
- Added date and author to layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/242c83c4ac45f7182944f6dfa964e0097284c87e)
- Okhttp by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e34c33b9ff27b1325c198b7986a8cec8efe4d2d7)
- Start generalize a bit before switching database by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0590f04f49ee873de88062a0536303729fad7a46)
- Fix misparse of youtube urls by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/05d48caec6efd83309b7ef1eaccdc2abc117d477)
- Update gradle by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d50fb821e48c6aa9716f586aadc5c2befa2d0204)
- Rest skeleton in flask up by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2b19f436d0a8836116160bcfe5064dc3f5e3efa9)
- Sqlalchemy db basically done by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/57dd875d7c4f03ef0336d9fd00cdd9027b13a9b2)
- There are tests! by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a05388d0870ece287e998544964a8bacf7e7e48e)
- Cascade and clean up function by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fadaa1e03955288ccd1998bbac0021cf62f01d7a)
- Roll my own HTML parser by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1fae032b9c909afc5b1950fe53e08b922f225ce1)
- Added auth and actually tested with app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/10529046ce901238f2a8462702bb76cb70ccc7cb)
- Changed to python server in app by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c8254315774d342f4773a889c8e1e458ecbbf5d3)
- Add appengine readme by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9d091aac746759aa27a8c390ded154ac5e8440ea)
- Compatible with uwsgi by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2053590e3ea4cc72e333791eab30b29f90039a8f)
- Add README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5ff07ecaaaf5656d6b9856b0bbc9426ab6f5ab9a)
- More readmes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/46261ec269aa4e8df63fc94c6aac4738bb8af77b)
- More readmes again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b31d79fb26c79192a5c700f5dda26aa248589845)
- Fix update of feeds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/feb9e823ad2931e16a62bf5e4931054dbbd99e43)
- Set published of feeditem to timestamp if necessary by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c5e2c9a8994a90672b8c22a62fd563a230367845)
- Fix crash with pending by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8120ce311ae971d982b2d0203eb46b5c9f940203)
- Dont crash if no pubdate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/09a862839fd302009e944eae1fb456af5f874152)
- SyncAdapter and applyBatch implemented by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4b1b71c49244041163c9e7b268c10575cbc1e93b)
- Fix enclosure link handling by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cf711fe0d9a390ce5aec8cded16610b3f4b8159c)
- Add periodic sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f73ba8d623b48a0fcce39a3c167eef76fdc02410)
- Fix periodic sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d17c47a2eee12bb7d578e6de50933fe49cd22b5e)
- Fix applyBatch backrefs by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c6c0bd97e836bc1b5f50c46a43f21e9e517d5034)
- Use https by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6031b2f9144df935ad4b7421dff44ca83d6129fd)
- Added swipe to refresh, no sync animations thought by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/526c384ff9a8f07554a2ff8ba17658cd560b9e4c)
- Remove swipe to refresh again\nWorks badly with overlayed action bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1c68eb8122f19fc29487ea88af01e0605a7ffcbd)
- Fix returning all feeditems by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c8a31ef776159508170f28168b08ce4def6904a1)
- Added missing cascades by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/61957a49617d912819ce393b706fe80df0c264b7)
- Some debug prints by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1785883e1c88bc26e0a97973534bc7cfa957bcbd)
- Print error by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9dcf788e96fe79e425e0228d93d0bfb99a6b7e2a)
- Missed id!! by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2ce6ef82e216f95cd512eccd32e4c720adbae321)
- Remove debug prints again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7890aab75563600a897176b70015adff0096a5a1)
- Make sure dt is defined by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e84c2f12cd5a669b23b0335591d3e3ab595491b0)
- Added progress bars for sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f018436611dec758d1d16f355aeebd796f880d3e)
- Add a sync delay, and report soft error is possible by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/264441325092edaa716256bb67913ce9604d7030)
- Implemented pending operations by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b3ce4a8ef22b523160e0622b56c05d59b6803094)
- Added some empty views by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/858d3d37cdf704a448695df8e6c2a6dae90d55f4)
- Improve empty screens, and sync after account selection by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8ab763a6f707ac614376054d6aeacd607aba4f82)
- Added tags auto suggestions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/63216748ee4b40b98fa537a41f2de7067a1d9273)
- Improved edit_feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/527eec3fff6cdd1ba7a1660bd8a97e5bb71fd5e1)
- Fix suggestions filter on start by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/55457f030696e392b408b59b66dda381a4cb2397)
- Cursor at end of text as it should by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/27f01fbe40fb0dae1c953cec65ed928538b02beb)
- Manual control of empty view to avoid flickering by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3638a7ac4cf65145e724f6c49dd8a6e73b4dd4f2)
- Display domains of links to be opened by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3bb696a81336ee6bd1cc5f0a26929e863a43a960)
- Replace ampersands in imageurls by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6ebea9a8a6242cd0efcaf05cf282bfb26b7315bb)
- Untested delete sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ce15c6f7773724c337208aa668b9f35ec05fc8b3)
- Fix indent and typo by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2f1a9f43c1982363839ad1887a696224f595194b)
- Forgotten import by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e65b5e6d692998d08c00d82913d746b4e9c55c6d)
- Print errors by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ddd5814c13f45e56e572e01660d4eda5efd7307e)
- Forgot to change response in server by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2e4860d4580a2d7f7257c03e76790eb7320a4c26)
- Try other syntax by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/750723c3c56de57d591f951159b47f42ce0a3a0f)
- Cant do unique like that by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9cb5f8e5f63745ce71f2644957a1784cc125018a)
- Used wrong marshal by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1b01d567a7b92fd0812ddbf70e7f46435a17c292)
- Unquote delete urls by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/62451a464e4277de19fc250de583c4c8a7ef5ba5)
- Debug prints by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a140cebc5cf3e24f0e5d68f991d7298c2a71e0dd)
- Prints print prints by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/04867e89a9a43c96ea8e4a60337cbc2faf4c7cea)
- Added proper logging by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0ddaa4d205a07e789f63960611819631d250ad31)
- Use post for delete by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/66b843e19dbf353c1902b0800646d66ed17ea542)
- Use new post delete by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/49cee7d4d3df7067d25fb258f208a05e9b533ceb)
- Log errors by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/df66ca97ccef0a9fde5bcfbd1fdd4949c7bb98c8)
- Default timestamp to 2 weeks ago by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a2cfea5925ff537bacbe6f6eea3ea9e80bf59a54)
- Change to 7 days by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/101ad0d9a3ca3cdb0b5b833b4fcfd90ebfb6f242)
- Moved to recyclerview now that lollipop is released by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0345f6370bc3c2287c22db12e7e58fb7d661e7a7)
- Use grid on larger devices by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4b47b9c02de170950662bbacca141a32540ebd36)
- Tweak grid a little by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c0035d1ed23315524b12ed0e9cb7d20e9f23d414)
- Material design style using appcompat by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f71b2373d3dfa3732627b9ac851de3c4d41cf87e)
- Add icons and string resources by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f9dca095696ef505ad47f74001f4fb0512277895)
- More string resource by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/b643543a8defde01990c0cc21726c97548bae0f2)
- Fix image widths by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ba732e1329068994b15932fc809b2da6da15dd74)
- Move drawable so its always found by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bd15138d4ceeee13ec1ae3b7f84167a735478ebc)
- Use new medium font by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dece07a3f8e442661009aa29dcd37cc34e928802)
- Add hidden snackbar, WIP by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/71378406621efc0c4033f58fa49d51a1edf09ed5)
- Add ellipsis to items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ca0e2060b1d822b659b3cebbe38446f1710e4fd3)
- New color divider by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/085f6d708aa691065b3943eb2caf5d6223853ad6)
- Improve divider by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/070c72515e1219d360aea59aba3ca175331f2265)
- Set title by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5c7632282f31269e3a72517dc7869d73f0476278)
- Fix unescaping by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6a865e1ebecd6f034d6ec2db41e18e0c334e33cb)
- Draw divider at top again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cf9106bc898d9de0a3541cf9a419176e040fe2b6)
- Smoother hiding of AB by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a76ea7e7204e96dd0fec62d75bf776dcc5e1ca31)
- Dont scale unknown images by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/57ee3f28cfde66f6405939c32497b0e4776883ed)
- Change color of FAB to accent by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/41bf8d80c2ee218393817c3d479253d2303c647b)
- App icon by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6746138ee8b359da9257397ee5942383669ad985)
- Load feed info on default load to get title by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9d8b8b54300ce3e6e29d302409078b230dc485f3)
- More sensible default image size by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/111331d9883b85440776fa3ab149a370fe9615ef)
- Use fit() for correct imageview size by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/656ad664628b3cb963c448716ae22389f8393dbb)
- Use my own quotespan by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8c6ef11ce6a9b70069df5901fdc33c6826f5feac)
- Better fix for imagesize problems by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9d58a2b303c06d22096cc48bae097765512e15d9)
- Change link color to accent color in feed view by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0033f34ef62c1c7e12324c9163e6e06480fc89f8)
- Change FAB color on tablets by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5b0d972c779e78d11660e96b6feccc4a6be7ae19)
- Scale image on tablets again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/88a4006e33ded624d58dc859b6b37cbf543f4fc9)
- Updated README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8d743d231bc4b967ad63530ef4051763c9bc2594)
- Add forgotten file... :/ by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/67a835952de6965992b7716dc911cc9a6d97b9a3)
- Managed to remove dependence on play services! by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e3895a42317257b836ead22ff7409c2bc9753704)
- Sync feed when added first time. by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/168275ace314e2da783057d556b3b525b6e84c29)
- Dont crash on account request, and reload on first sync when account exists by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/481389c520c040341cad6500b30f43b838986506)
- Almost done with server editor by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d2eb756e071f186098cd9543384a0d8d7803f5a1)
- Update todos by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/51405b78e4d41a3b70df14e6fb188af150e279dd)
- Listen for changes to user by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5bbb9d32dbfc20dce422ee5fbf954104e961e819)
- Add Flattr to README by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/43c3747862921bf79de2657e360e05d80d770f5f)
- Allow user to configure server by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a655536ef0601a151b573d8511fbb487645cc33f)
- Fix action bar appearing above toolbar when selecting text for example by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a9d64002594f021efeaf2603aded6763eb9b3009)
- Use new Picasso method to pause/cancel requests by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fd0a03fdc016fc49d4aae870ccb212dcdf843603)
- Use a custom layout for handling list items. by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/59ffd129f8a5f03fdc1a0c79094240811ea1b121)
- Fix crash when no network by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/10d43c7d3b46dedee82bd576feb5adbf74df54b5)
- Faster debug build times by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/02cf5f39fbc739b36212d89a6d77d4c93a96121c)
- Update to Studio 1.0 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/93706632d16189d66b75a5500d840a6617c8cd02)
- Trying to fix height of images by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e6643e6e5b071cc5c7c42b9e7ef1dfc3dd34d061)
- Little cleanup by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d3a72e16a34e499c571a2a429689e451e0937eb1)
- Add a play media button for reader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2bb0b02a7af88d37b329c72a1c62e5464d50f81d)
- Add filename to enclosure tooltip by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e2c2617bfd70d4588cb4ca83c98f2c624040437a)
- Show filename for enclosure items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6f8e6e21040d7b2a10db2dec20a118fa39987a73)
- Always show action bar on resume, to avoid a hiding bug by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/733a32285a090f86f8bc257610ef1c5331db9367)
- Add URL to edit feed window. Add floatlabels too by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d85b48ea51bc20555b63e623316bcc3930f1bc4a)
- New based on feature. by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/f9411752ec09056942521ba7ca21e611572953f4)
- Show URL, but dont allow it to be changed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a915193109f9719c3315759f7aad7f2c8fa6c5b8)
- Add DB and GUI for notification toggles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/873477db42bff0b86ca7f7a60581c5214adee7ae)
- Add notifications. Fixes #3 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/95389caa714d0f0bfd9257982244c9cb0821b5d5)
- Notify toggle button. Database service by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8d3e144c3906fd7b7d2058f917830838afac26f2)
- Show current mode in icon by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7531d9d1317511c7723ba00c0398fde7b262008e)
- Fix scrolling for new toolbar height by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5369335388aa10a56210c3f2f85c4299d909f186)
- Show toolbar when bottom is reached by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/989aa194a50a29f97cb59bc30d97474cde81bf88)
- Remove settings entry by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d4f2c0531f70af75401f79f22701a914d9d524eb)
- Use big text notification for single items by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ddc8d688c53bad4aa25cac89ca3a8f09560234e9)
- Use yaml config by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/087ee6d44626e443c1ee022f4c85f87c6f863b00)
- Add feedtitle to notification lines by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/43b3ad8131f482bc25ae40a60a5deecbd8e34159)
- Just check plaintext passwords directly by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/69493bdc0a1a6d9e000e072ae0119e0f59d37cb0)
- Run sync with configfile by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/cfb87f725c6bc11dab2649c4875101878ea69cc9)
- Forgot file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/839ab228e826732add982d3cf19cbdbed4ca9a87)
- Case in uswgi and config by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7f4cf349226cf4b7eecd9e2da4afe0585e846867)
- Load database at correct time in import order by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ef4082c2f511146e12a42c7c37a5cd41796303ea)
- Load config file for uwsgi by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1c54d76950a695c3ec84813ed9e21e91c34393d8)
- Remove default object by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5787774e3de510c926f56ef867437d0ee269c4d7)
- Port must be numeric by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c204f75f12f3bbf4bb0797cd78fec48f79231350)
- Fix import order by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a1279616ce9685d06d04e658e907ecff2fdd72f2)
- Fix import orers again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/311a4acfb227c91bf6fe4298341b5dcdb017c614)
- Fix sync script by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4dec62aa5575cecdfcd7b6fa9a24783d92279625)
- Added pyyaml to requirements by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4cb406c3e77d6ecf0856a6d1ebf6a62ed85cb978)
- Added docker stuff by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4502c7a7501ac448f42c9588fc27b71087f66dbb)
- Basic file reader/writer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/35f2c2110df48743bf4cad05cff08f7f98d82f1a)
- OPML classes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/952d3805fca75c251a2d542dd28005dc293c6230)
- OMPL import/export fixes #5 by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/6c14766429262d9916d5ddf3c4f425f5479cb161)
- Added screenshots by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/eeb546468b443ef52c67d0dc6018f5dd03d20bcf)
- Update readme by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/abc26bf71a8c324346e127fdd9b11b1234eedd6f)
- Black colors by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fb82a0629722f47254799139d5630d7e4d75797e)
- Use Glide instead of Picasso by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7ba1b2bc4e49c017ed93dfba7428a7194443c6ab)
- Use switch statement here by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7093613e55d6c18de448e38a814ef02e38450bda)
- Rename these entries by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9ba33a9a28d78f7eeb42443fb41b5c19be0699f8)
- Contextual action bar by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/71c140ba612129b7d4e349bd4d9158e2eb33adb0)
- First implementation of a DeltaLoader by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/aa420fd73ca0b3b675c513e14a3b84e5dd2246c4)
- Fix endless loading by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/71e4d20ea1a59f14447fb9dc2f462bdc9b7cc033)
- Now possible to swipe to toggle readstate by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/80fbf6ca3ef9bae739aa7cd73b078e7b8bf652fb)
- Change alpha interpolation on swipe by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/668452f9c6f3eb4c0994299a545369b37b6afb18)
- Save original item as json field by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/06f1085e655309f78606c98384007bcd4b8a4529)
- Add json field by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a466df2d1470c55dc9374e7922958292b13138c5)
- Clear old items on sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ee3ec7c172f216ca2bcfd7b68e0d58c8dd835c3f)
- Set build versions by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e09c6fdb6298c2baa0f90fb11a6583de0c2a4cad)
- Use magnet links when available by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/45f1f840d6ce6cc02db13c16efeab5a4ee286e9f)
- Display more info by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bf454e49095493bba310af2394aa65c4f581433c)
- Parse JSON in background by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/1b61a4392f841151fcd99e2e1d114811af2a295f)
- Swipe refresh layout by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/96335a8c084e5469dfd7f1d6fc42a897afc08566)
- Tinted expand indicators by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/60af6724c158327b7ea5da05e59ff6190e6e9f3a)
- Dialog backgrounds by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/121a1bdb338762c099e6812f0c41835164da1643)
- Show hamburger icon again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9f296567ce5725f561d66f28ec997849187d15ef)
- Correct up navigation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/860ec9de8692f272e8ebf4aaf3bca0e9355730b7)
- Shared element transition by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/bbf004cb8f8ca415002ee2a332b43092d5157b86)
- A swiping item background by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/35be26d18a27d0ebc5bfc821986bd04adfe02168)
- Toggleable night mode by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d5e2358aa0c94339083bcf6477b60c85cdda26e3)
- Remove activity transition, was slow as fuck by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/81e8389ba63c95974b30c706e3e3a7b2e84974a0)
- Increase cleaning speed dramatically by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e71cad208c868d957efdc382dc1271a38f93a35b)
- Catch error by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/243bca43b7f4cf23105db7ab417ca6d927ca04fd)
- Catch another error by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/173e1c3fbe77bfa9b81bff38a953300d79ffee90)
- CreateDB script funtional again by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/14ad839dccf431042556f5b3888fea856ef21833)
- Add indexes by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7fef64969810704405a4671fd1b77392c32536c0)
- Script for running one-time SQL on upgrades by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/23b12cedde8da077111f9a78c86aa24f6e92e978)
- Use an expandable recyclerview for nav-drawer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0a361473cd6ca91a25456c454926d677101a00f3)
- Parent now also updates by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/156188830352a77541f277ac95564dd5da0a118e)
- Hide unread counts if zero by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7a63a2fe00b7034a9f799d080aafc80e5c896451)
- Use invisible instead of gone to get padding by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/74c19fceae432376ab457f3048459dfca00244df)
- Moving some stuff to design library by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/dbb39d4479781ee398f9f07bb74183878859e09e)
- Use query param to limit to 50 stories by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/40c01f8f9498c3c05088da726005574bfadd5c43)
- Timeout if server doesnt respond by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/18c294e4ac9e872d118c6ab220f23dc8b5af5510)
- Fix the sorting of the ExpandableAdapter by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4836520ab798e878c37e1e97aa3d47db90c2ce45)
- Make the sorting algorithm look nicer by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d7d963ea6464f861bcc42bffaeb0f21c288c557a)
- Avoid bad titles by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/55e2c3d170df06711f629c39e7055df1ec0127dc)
- Add crontab description by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/4d6d6de07a78dc6e6c21731d99f324bf2f912d79)
- Docker files by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/10af8af9d4180201674141bbd8f75104bbdc2fb3)
- Add cron example by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/05ae5d60b892c750732588ef76ad6e05c851f120)
- Actually allow plain text password for testing by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0b96d618308b91e773c97c2da4faed1b11f88297)
- Fix sending no title on add feed by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/0bc15e84a42ce89ceaeb86325f0c1b52bb877740)
- Add GUID to database and return statement by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/80b072f46c9e93382feada7d6b2b0f894aa56828)
- Add GUID to sync by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/623253512406cc3ac5c377e481956b80516a709b)
- Prune feed items 51 and beyond by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/38051be70dd74cf725d70c27eeb78df3b1af4cde)
- Improve testing and fix OMPL escaping by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/ad27c5426e5e03fb1e5f93e52f8d69a191c950ec)
- Add a drone file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/de4009697aab14b1c9a671459f8f509b936aca7a)
- Need git inside image by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d901038abf8aac58e163f9b78062b82e0abb270a)
- Try specific tag of restful by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c9b8ec60cd885f28f9f31093ddf8e688867958df)
- There is no setup.py, and add version matrix by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/a4f0e76e9437de869b472713e914c5f309adbfec)
- Update sync to be threaded (with timeouts) by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/7101afb4812bc74e2c7a9df866dd2a9f0637ab9c)
- Remove appengine dir by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/fc6de2f61b0de98e3eb711e90d353ea61ecb49c3)
- Catch error on database vacuum by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/9289cf54ca0fb276b42d15e466f0752a6d6568a0)
- Update readme, createdb and runserver scripts by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/e3a5a386b3b7ec73e9b9f29c7644023091bb19c5)
- Add an entrypoint for easy use by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/2f4601148e76da869e038d1988df7258007a4cf3)
- Make sql single-threaded by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/3f457ed07dd24ff65ba600f36dae79c29e502f94)
- Debian stuff by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c134ec82a3de8fab234dae73b86654f7f73f3aed)
- Debian python build by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/efac0200bdaa3c912435774445a8d7d33a54d20a)
- Better script file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/5756e0ec18c3b0037f906becdef1b06459188256)
- Remove deprecation warnings by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/c6d9ff12a823a08c7269e6821f0e45356189a59e)
- Debian package done by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/afc65181edc88eab085bb41a2db931241bc04b06)
- Update make file by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/8056404b8740a500cf44377a137aa5b16a2d5eef)
- Improve man page generation by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/d77c72790bd046a8ba0956cc580e1812fde3463f)
- Update changelog by @spacecowboy in [commit](https://github.com/spacecowboy/feeder/commit/effc7bafcf8ce2c72735d124e761220cf0d3d401)

### ❤️  New Contributors
* @spacecowboy made their first contribution

[2.9.1]: https://github.com/spacecowboy/feeder/compare/2.9.0..2.9.1
[2.9.0]: https://github.com/spacecowboy/feeder/compare/2.8.1..2.9.0
[2.8.1]: https://github.com/spacecowboy/feeder/compare/2.8.0..2.8.1
[2.8.0]: https://github.com/spacecowboy/feeder/compare/2.7.4..2.8.0
[2.7.4]: https://github.com/spacecowboy/feeder/compare/2.7.3..2.7.4
[2.7.3]: https://github.com/spacecowboy/feeder/compare/2.7.2..2.7.3
[2.7.2]: https://github.com/spacecowboy/feeder/compare/2.7.1..2.7.2
[2.7.1]: https://github.com/spacecowboy/feeder/compare/2.7.0..2.7.1
[2.7.0]: https://github.com/spacecowboy/feeder/compare/2.6.33..2.7.0
[2.6.33]: https://github.com/spacecowboy/feeder/compare/2.6.32..2.6.33
[2.6.32]: https://github.com/spacecowboy/feeder/compare/2.6.31..2.6.32
[2.6.31]: https://github.com/spacecowboy/feeder/compare/2.6.30..2.6.31
[2.6.30]: https://github.com/spacecowboy/feeder/compare/2.6.29..2.6.30
[2.6.29]: https://github.com/spacecowboy/feeder/compare/2.6.28..2.6.29
[2.6.28]: https://github.com/spacecowboy/feeder/compare/2.6.27..2.6.28
[2.6.27]: https://github.com/spacecowboy/feeder/compare/2.6.26..2.6.27
[2.6.26]: https://github.com/spacecowboy/feeder/compare/2.6.25..2.6.26
[2.6.25]: https://github.com/spacecowboy/feeder/compare/2.6.24..2.6.25
[2.6.24]: https://github.com/spacecowboy/feeder/compare/2.6.23..2.6.24
[2.6.23]: https://github.com/spacecowboy/feeder/compare/2.6.22..2.6.23
[2.6.22]: https://github.com/spacecowboy/feeder/compare/2.6.21..2.6.22
[2.6.21]: https://github.com/spacecowboy/feeder/compare/2.6.20..2.6.21
[2.6.20]: https://github.com/spacecowboy/feeder/compare/2.6.19..2.6.20
[2.6.19]: https://github.com/spacecowboy/feeder/compare/2.6.18..2.6.19
[2.6.18]: https://github.com/spacecowboy/feeder/compare/2.6.17..2.6.18
[2.6.17]: https://github.com/spacecowboy/feeder/compare/2.6.16..2.6.17
[2.6.16]: https://github.com/spacecowboy/feeder/compare/2.6.15..2.6.16
[2.6.15]: https://github.com/spacecowboy/feeder/compare/2.6.14..2.6.15
[2.6.14]: https://github.com/spacecowboy/feeder/compare/2.6.13..2.6.14
[2.6.13]: https://github.com/spacecowboy/feeder/compare/2.6.12..2.6.13
[2.6.12]: https://github.com/spacecowboy/feeder/compare/2.6.11..2.6.12
[2.6.11]: https://github.com/spacecowboy/feeder/compare/2.6.10..2.6.11
[2.6.10]: https://github.com/spacecowboy/feeder/compare/2.6.9..2.6.10
[2.6.9]: https://github.com/spacecowboy/feeder/compare/2.6.8..2.6.9
[2.6.8]: https://github.com/spacecowboy/feeder/compare/2.6.7-1..2.6.8
[2.6.7-1]: https://github.com/spacecowboy/feeder/compare/2.6.7..2.6.7-1
[2.6.7]: https://github.com/spacecowboy/feeder/compare/2.6.6..2.6.7
[2.6.6]: https://github.com/spacecowboy/feeder/compare/2.6.5..2.6.6
[2.6.5]: https://github.com/spacecowboy/feeder/compare/2.6.4..2.6.5
[2.6.4]: https://github.com/spacecowboy/feeder/compare/2.6.3..2.6.4
[2.6.3]: https://github.com/spacecowboy/feeder/compare/2.6.2..2.6.3
[2.6.2]: https://github.com/spacecowboy/feeder/compare/2.6.1..2.6.2
[2.6.1]: https://github.com/spacecowboy/feeder/compare/2.6.0..2.6.1
[2.6.0]: https://github.com/spacecowboy/feeder/compare/2.5.0..2.6.0
[2.5.0]: https://github.com/spacecowboy/feeder/compare/2.4.20..2.5.0
[2.4.20]: https://github.com/spacecowboy/feeder/compare/2.4.19..2.4.20
[2.4.19]: https://github.com/spacecowboy/feeder/compare/2.4.18..2.4.19
[2.4.18]: https://github.com/spacecowboy/feeder/compare/2.4.17..2.4.18
[2.4.17]: https://github.com/spacecowboy/feeder/compare/2.4.16..2.4.17
[2.4.16]: https://github.com/spacecowboy/feeder/compare/2.4.15..2.4.16
[2.4.15]: https://github.com/spacecowboy/feeder/compare/2.4.14..2.4.15
[2.4.14]: https://github.com/spacecowboy/feeder/compare/2.4.13..2.4.14
[2.4.13]: https://github.com/spacecowboy/feeder/compare/2.4.12..2.4.13
[2.4.12]: https://github.com/spacecowboy/feeder/compare/2.4.11..2.4.12
[2.4.11]: https://github.com/spacecowboy/feeder/compare/2.4.10..2.4.11
[2.4.10]: https://github.com/spacecowboy/feeder/compare/2.4.9..2.4.10
[2.4.9]: https://github.com/spacecowboy/feeder/compare/2.4.8..2.4.9
[2.4.8]: https://github.com/spacecowboy/feeder/compare/2.4.7..2.4.8
[2.4.7]: https://github.com/spacecowboy/feeder/compare/2.4.6-2..2.4.7
[2.4.6-2]: https://github.com/spacecowboy/feeder/compare/2.4.6-1..2.4.6-2
[2.4.6-1]: https://github.com/spacecowboy/feeder/compare/2.4.6..2.4.6-1
[2.4.6]: https://github.com/spacecowboy/feeder/compare/2.4.5..2.4.6
[2.4.5]: https://github.com/spacecowboy/feeder/compare/2.4.4..2.4.5
[2.4.4]: https://github.com/spacecowboy/feeder/compare/2.4.3..2.4.4
[2.4.3]: https://github.com/spacecowboy/feeder/compare/2.4.2..2.4.3
[2.4.2]: https://github.com/spacecowboy/feeder/compare/2.4.1..2.4.2
[2.4.1]: https://github.com/spacecowboy/feeder/compare/2.4.0..2.4.1
[2.4.0]: https://github.com/spacecowboy/feeder/compare/2.3.9..2.4.0
[2.3.9]: https://github.com/spacecowboy/feeder/compare/2.3.8..2.3.9
[2.3.8]: https://github.com/spacecowboy/feeder/compare/2.3.7..2.3.8
[2.3.7]: https://github.com/spacecowboy/feeder/compare/2.3.6..2.3.7
[2.3.6]: https://github.com/spacecowboy/feeder/compare/2.3.5..2.3.6
[2.3.5]: https://github.com/spacecowboy/feeder/compare/2.3.4..2.3.5
[2.3.4]: https://github.com/spacecowboy/feeder/compare/2.3.3..2.3.4
[2.3.3]: https://github.com/spacecowboy/feeder/compare/2.3.2..2.3.3
[2.3.2]: https://github.com/spacecowboy/feeder/compare/2.3.1..2.3.2
[2.3.1]: https://github.com/spacecowboy/feeder/compare/2.3.0..2.3.1
[2.3.0]: https://github.com/spacecowboy/feeder/compare/2.2.7..2.3.0
[2.2.7]: https://github.com/spacecowboy/feeder/compare/2.2.6..2.2.7
[2.2.6]: https://github.com/spacecowboy/feeder/compare/2.2.5..2.2.6
[2.2.5]: https://github.com/spacecowboy/feeder/compare/2.2.4-1..2.2.5
[2.2.4-1]: https://github.com/spacecowboy/feeder/compare/2.2.4..2.2.4-1
[2.2.4]: https://github.com/spacecowboy/feeder/compare/2.2.3..2.2.4
[2.2.3]: https://github.com/spacecowboy/feeder/compare/2.2.2..2.2.3
[2.2.2]: https://github.com/spacecowboy/feeder/compare/2.2.1..2.2.2
[2.2.1]: https://github.com/spacecowboy/feeder/compare/2.2.0..2.2.1
[2.2.0]: https://github.com/spacecowboy/feeder/compare/2.1.8..2.2.0
[2.1.8]: https://github.com/spacecowboy/feeder/compare/2.1.7..2.1.8
[2.1.7]: https://github.com/spacecowboy/feeder/compare/2.1.6..2.1.7
[2.1.6]: https://github.com/spacecowboy/feeder/compare/2.1.5..2.1.6
[2.1.5]: https://github.com/spacecowboy/feeder/compare/2.1.4..2.1.5
[2.1.4]: https://github.com/spacecowboy/feeder/compare/2.1.3..2.1.4
[2.1.3]: https://github.com/spacecowboy/feeder/compare/2.1.2..2.1.3
[2.1.2]: https://github.com/spacecowboy/feeder/compare/2.1.1..2.1.2
[2.1.1]: https://github.com/spacecowboy/feeder/compare/2.1.0-1..2.1.1
[2.1.0]: https://github.com/spacecowboy/feeder/compare/2.0.14..2.1.0
[2.0.14]: https://github.com/spacecowboy/feeder/compare/2.0.13..2.0.14
[2.0.13]: https://github.com/spacecowboy/feeder/compare/2.0.12..2.0.13
[2.0.12]: https://github.com/spacecowboy/feeder/compare/2.0.11..2.0.12
[2.0.11]: https://github.com/spacecowboy/feeder/compare/2.0.10..2.0.11
[2.0.10]: https://github.com/spacecowboy/feeder/compare/2.0.9..2.0.10
[2.0.9]: https://github.com/spacecowboy/feeder/compare/2.0.8..2.0.9
[2.0.8]: https://github.com/spacecowboy/feeder/compare/2.0.7..2.0.8
[2.0.7]: https://github.com/spacecowboy/feeder/compare/2.0.6..2.0.7
[2.0.6]: https://github.com/spacecowboy/feeder/compare/2.0.5..2.0.6
[2.0.5]: https://github.com/spacecowboy/feeder/compare/2.0.4..2.0.5
[2.0.4]: https://github.com/spacecowboy/feeder/compare/2.0.3..2.0.4
[2.0.3]: https://github.com/spacecowboy/feeder/compare/2.0.2..2.0.3
[2.0.2]: https://github.com/spacecowboy/feeder/compare/2.0.1..2.0.2
[2.0.1]: https://github.com/spacecowboy/feeder/compare/2.0.0..2.0.1
[2.0.0]: https://github.com/spacecowboy/feeder/compare/2.0.0-rc.4..2.0.0
[2.0.0-rc.4]: https://github.com/spacecowboy/feeder/compare/2.0.0-rc.3..2.0.0-rc.4
[2.0.0-rc.3]: https://github.com/spacecowboy/feeder/compare/2.0.0-rc.2..2.0.0-rc.3
[2.0.0-rc.2]: https://github.com/spacecowboy/feeder/compare/2.0.0-rc.1..2.0.0-rc.2
[2.0.0-rc.1]: https://github.com/spacecowboy/feeder/compare/2.0.0-beta.6..2.0.0-rc.1
[2.0.0-beta.6]: https://github.com/spacecowboy/feeder/compare/2.0.0-beta.5..2.0.0-beta.6
[2.0.0-beta.5]: https://github.com/spacecowboy/feeder/compare/2.0.0-beta.4..2.0.0-beta.5
[2.0.0-beta.4]: https://github.com/spacecowboy/feeder/compare/2.0.0-beta.3..2.0.0-beta.4
[2.0.0-beta.3]: https://github.com/spacecowboy/feeder/compare/2.0.0-beta.2..2.0.0-beta.3
[2.0.0-beta.2]: https://github.com/spacecowboy/feeder/compare/2.0.0-beta.1..2.0.0-beta.2
[2.0.0-beta.1]: https://github.com/spacecowboy/feeder/compare/1.13.5..2.0.0-beta.1
[1.13.5]: https://github.com/spacecowboy/feeder/compare/1.13.4..1.13.5
[1.13.4]: https://github.com/spacecowboy/feeder/compare/1.13.3..1.13.4
[1.13.3]: https://github.com/spacecowboy/feeder/compare/1.13.2..1.13.3
[1.13.2]: https://github.com/spacecowboy/feeder/compare/1.13.1..1.13.2
[1.13.1]: https://github.com/spacecowboy/feeder/compare/1.13.0..1.13.1
[1.13.0]: https://github.com/spacecowboy/feeder/compare/1.12.1..1.13.0
[1.12.1]: https://github.com/spacecowboy/feeder/compare/1.12.0..1.12.1
[1.12.0]: https://github.com/spacecowboy/feeder/compare/1.11.3..1.12.0
[1.11.3]: https://github.com/spacecowboy/feeder/compare/1.11.2..1.11.3
[1.11.2]: https://github.com/spacecowboy/feeder/compare/1.11.1..1.11.2
[1.11.1]: https://github.com/spacecowboy/feeder/compare/1.11.0..1.11.1
[1.11.0]: https://github.com/spacecowboy/feeder/compare/1.10.14..1.11.0
[1.10.14]: https://github.com/spacecowboy/feeder/compare/1.10.13..1.10.14
[1.10.13]: https://github.com/spacecowboy/feeder/compare/1.10.12..1.10.13
[1.10.12]: https://github.com/spacecowboy/feeder/compare/1.10.11..1.10.12
[1.10.11]: https://github.com/spacecowboy/feeder/compare/1.10.10..1.10.11
[1.10.10]: https://github.com/spacecowboy/feeder/compare/1.10.9..1.10.10
[1.10.9]: https://github.com/spacecowboy/feeder/compare/1.10.8..1.10.9
[1.10.8]: https://github.com/spacecowboy/feeder/compare/1.10.7..1.10.8
[1.10.7]: https://github.com/spacecowboy/feeder/compare/1.10.6..1.10.7
[1.10.6]: https://github.com/spacecowboy/feeder/compare/1.10.5..1.10.6
[1.10.5]: https://github.com/spacecowboy/feeder/compare/1.10.4..1.10.5
[1.10.4]: https://github.com/spacecowboy/feeder/compare/1.10.3..1.10.4
[1.10.3]: https://github.com/spacecowboy/feeder/compare/1.10.2..1.10.3
[1.10.2]: https://github.com/spacecowboy/feeder/compare/1.10.1..1.10.2
[1.10.1]: https://github.com/spacecowboy/feeder/compare/1.10.0..1.10.1
[1.10.0]: https://github.com/spacecowboy/feeder/compare/1.9.9..1.10.0
[1.9.9]: https://github.com/spacecowboy/feeder/compare/1.9.8..1.9.9
[1.9.8]: https://github.com/spacecowboy/feeder/compare/1.9.7..1.9.8
[1.9.7]: https://github.com/spacecowboy/feeder/compare/1.9.6..1.9.7
[1.9.6]: https://github.com/spacecowboy/feeder/compare/1.9.5..1.9.6
[1.9.5]: https://github.com/spacecowboy/feeder/compare/1.9.4..1.9.5
[1.9.4]: https://github.com/spacecowboy/feeder/compare/1.9.3..1.9.4
[1.9.3]: https://github.com/spacecowboy/feeder/compare/1.9.2..1.9.3
[1.9.2]: https://github.com/spacecowboy/feeder/compare/1.9.1..1.9.2
[1.9.1]: https://github.com/spacecowboy/feeder/compare/1.9.0..1.9.1
[1.9.0]: https://github.com/spacecowboy/feeder/compare/1.8.30..1.9.0
[1.8.30]: https://github.com/spacecowboy/feeder/compare/1.8.29..1.8.30
[1.8.29]: https://github.com/spacecowboy/feeder/compare/1.8.28..1.8.29
[1.8.28]: https://github.com/spacecowboy/feeder/compare/1.8.27..1.8.28
[1.8.27]: https://github.com/spacecowboy/feeder/compare/1.8.26..1.8.27
[1.8.26]: https://github.com/spacecowboy/feeder/compare/1.8.25..1.8.26
[1.8.25]: https://github.com/spacecowboy/feeder/compare/1.8.24..1.8.25
[1.8.24]: https://github.com/spacecowboy/feeder/compare/1.8.23..1.8.24
[1.8.23]: https://github.com/spacecowboy/feeder/compare/1.8.22..1.8.23
[1.8.22]: https://github.com/spacecowboy/feeder/compare/1.8.21..1.8.22
[1.8.21]: https://github.com/spacecowboy/feeder/compare/1.8.20..1.8.21
[1.8.20]: https://github.com/spacecowboy/feeder/compare/1.8.19..1.8.20
[1.8.19]: https://github.com/spacecowboy/feeder/compare/1.8.18..1.8.19
[1.8.18]: https://github.com/spacecowboy/feeder/compare/1.8.17..1.8.18
[1.8.17]: https://github.com/spacecowboy/feeder/compare/1.8.16..1.8.17
[1.8.16]: https://github.com/spacecowboy/feeder/compare/1.8.15..1.8.16
[1.8.15]: https://github.com/spacecowboy/feeder/compare/1.8.14..1.8.15
[1.8.14]: https://github.com/spacecowboy/feeder/compare/1.8.13..1.8.14
[1.8.13]: https://github.com/spacecowboy/feeder/compare/1.8.12..1.8.13
[1.8.12]: https://github.com/spacecowboy/feeder/compare/1.8.11..1.8.12
[1.8.11]: https://github.com/spacecowboy/feeder/compare/1.8.10..1.8.11
[1.8.10]: https://github.com/spacecowboy/feeder/compare/1.8.9..1.8.10
[1.8.9]: https://github.com/spacecowboy/feeder/compare/1.8.8..1.8.9
[1.8.8]: https://github.com/spacecowboy/feeder/compare/1.8.7..1.8.8
[1.8.7]: https://github.com/spacecowboy/feeder/compare/1.8.6..1.8.7
[1.8.6]: https://github.com/spacecowboy/feeder/compare/1.8.5..1.8.6
[1.8.5]: https://github.com/spacecowboy/feeder/compare/1.8.4..1.8.5
[1.8.4]: https://github.com/spacecowboy/feeder/compare/1.8.3..1.8.4
[1.8.3]: https://github.com/spacecowboy/feeder/compare/1.8.2..1.8.3
[1.8.2]: https://github.com/spacecowboy/feeder/compare/1.8.1..1.8.2
[1.8.1]: https://github.com/spacecowboy/feeder/compare/1.8.0..1.8.1
[1.8.0]: https://github.com/spacecowboy/feeder/compare/1.7.1..1.8.0
[1.7.1]: https://github.com/spacecowboy/feeder/compare/1.7.0..1.7.1
[1.7.0]: https://github.com/spacecowboy/feeder/compare/1.6.8..1.7.0
[1.6.8]: https://github.com/spacecowboy/feeder/compare/1.6.7..1.6.8
[1.6.7]: https://github.com/spacecowboy/feeder/compare/1.6.6..1.6.7
[1.6.6]: https://github.com/spacecowboy/feeder/compare/1.6.5..1.6.6
[1.6.5]: https://github.com/spacecowboy/feeder/compare/1.6.4..1.6.5
[1.6.4]: https://github.com/spacecowboy/feeder/compare/1.6.3..1.6.4
[1.6.3]: https://github.com/spacecowboy/feeder/compare/1.6.2..1.6.3
[1.6.2]: https://github.com/spacecowboy/feeder/compare/1.6.1..1.6.2
[1.6.1]: https://github.com/spacecowboy/feeder/compare/1.6.0..1.6.1
[1.6.0]: https://github.com/spacecowboy/feeder/compare/1.5.0-1..1.6.0
[1.5.0-1]: https://github.com/spacecowboy/feeder/compare/1.5.0..1.5.0-1
[1.5.0]: https://github.com/spacecowboy/feeder/compare/1.4.3..1.5.0
[1.4.3]: https://github.com/spacecowboy/feeder/compare/1.4.2..1.4.3
[1.4.2]: https://github.com/spacecowboy/feeder/compare/1.4.1..1.4.2
[1.4.1]: https://github.com/spacecowboy/feeder/compare/1.4.0..1.4.1
[1.4.0]: https://github.com/spacecowboy/feeder/compare/1.3.15..1.4.0
[1.3.15]: https://github.com/spacecowboy/feeder/compare/1.3.14..1.3.15
[1.3.14]: https://github.com/spacecowboy/feeder/compare/1.3.13..1.3.14
[1.3.13]: https://github.com/spacecowboy/feeder/compare/1.3.12..1.3.13
[1.3.12]: https://github.com/spacecowboy/feeder/compare/1.3.11..1.3.12
[1.3.11]: https://github.com/spacecowboy/feeder/compare/1.3.10..1.3.11
[1.3.10]: https://github.com/spacecowboy/feeder/compare/1.3.9..1.3.10
[1.3.9]: https://github.com/spacecowboy/feeder/compare/1.3.8..1.3.9
[1.3.8]: https://github.com/spacecowboy/feeder/compare/1.3.7..1.3.8
[1.3.7]: https://github.com/spacecowboy/feeder/compare/1.3.6..1.3.7
[1.3.6]: https://github.com/spacecowboy/feeder/compare/1.3.5..1.3.6
[1.3.5]: https://github.com/spacecowboy/feeder/compare/1.3.4..1.3.5
[1.3.4]: https://github.com/spacecowboy/feeder/compare/1.3.3..1.3.4
[1.3.3]: https://github.com/spacecowboy/feeder/compare/1.3.2..1.3.3
[1.3.2]: https://github.com/spacecowboy/feeder/compare/1.3.1..1.3.2
[1.3.1]: https://github.com/spacecowboy/feeder/compare/1.3.0..1.3.1
[1.3.0]: https://github.com/spacecowboy/feeder/compare/1.2.3..1.3.0
[1.2.3]: https://github.com/spacecowboy/feeder/compare/1.2.2..1.2.3
[1.2.2]: https://github.com/spacecowboy/feeder/compare/1.2.1..1.2.2
[1.2.1]: https://github.com/spacecowboy/feeder/compare/1.2.0..1.2.1
[1.2.0]: https://github.com/spacecowboy/feeder/compare/1.1.2..1.2.0
[1.1.2]: https://github.com/spacecowboy/feeder/compare/1.1.1..1.1.2
[1.1.1]: https://github.com/spacecowboy/feeder/compare/1.1.0..1.1.1
[1.1.0]: https://github.com/spacecowboy/feeder/compare/migrate..1.1.0

<!-- generated by git-cliff -->
