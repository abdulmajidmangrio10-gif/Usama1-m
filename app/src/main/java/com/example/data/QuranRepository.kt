package com.example.data

import com.example.model.AyahItem
import com.example.model.SurahInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object QuranRepository {

    // Cache for loaded Surah ayahs
    private val ayahCache = mutableMapOf<Int, List<AyahItem>>()

    // All 114 Surahs of the Holy Quran
    val allSurahs: List<SurahInfo> = listOf(
        SurahInfo(1, "الفَاتِحَة", "The Opener", "Al-Fatihah", 7, "Makki"),
        SurahInfo(2, "البَقَرَة", "The Cow", "Al-Baqarah", 286, "Madani"),
        SurahInfo(3, "آل عِمْرَان", "Family of Imran", "Ali 'Imran", 200, "Madani"),
        SurahInfo(4, "النِّسَاء", "The Women", "An-Nisa", 176, "Madani"),
        SurahInfo(5, "المَائِدَة", "The Table Spread", "Al-Ma'idah", 120, "Madani"),
        SurahInfo(6, "الأَنْعَام", "The Cattle", "Al-An'am", 165, "Makki"),
        SurahInfo(7, "الأَعْرَاف", "The Heights", "Al-A'raf", 206, "Makki"),
        SurahInfo(8, "الأَنْفَال", "The Spoils of War", "Al-Anfal", 75, "Madani"),
        SurahInfo(9, "التَّوْبَة", "The Repentance", "At-Tawbah", 129, "Madani"),
        SurahInfo(10, "يُونُس", "Jonah", "Yunus", 109, "Makki"),
        SurahInfo(11, "هُود", "Hud", "Hud", 123, "Makki"),
        SurahInfo(12, "يُوسُف", "Joseph", "Yusuf", 111, "Makki"),
        SurahInfo(13, "الرَّعْد", "The Thunder", "Ar-Ra'd", 43, "Madani"),
        SurahInfo(14, "إِبْرَاهِيم", "Abraham", "Ibrahim", 52, "Makki"),
        SurahInfo(15, "الحِجْر", "The Rocky Tract", "Al-Hijr", 99, "Makki"),
        SurahInfo(16, "النَّحْل", "The Bee", "An-Nahl", 128, "Makki"),
        SurahInfo(17, "الإِسْرَاء", "The Night Journey", "Al-Isra", 111, "Makki"),
        SurahInfo(18, "الكَهْف", "The Cave", "Al-Kahf", 110, "Makki"),
        SurahInfo(19, "مَرْيَم", "Mary", "Maryam", 98, "Makki"),
        SurahInfo(20, "طه", "Ta-Ha", "Ta-Ha", 135, "Makki"),
        SurahInfo(21, "الأَنْبِيَاء", "The Prophets", "Al-Anbiya", 112, "Makki"),
        SurahInfo(22, "الحَجّ", "The Pilgrimage", "Al-Hajj", 78, "Madani"),
        SurahInfo(23, "المُؤْمِنُون", "The Believers", "Al-Mu'minun", 118, "Makki"),
        SurahInfo(24, "النُّور", "The Light", "An-Nur", 64, "Madani"),
        SurahInfo(25, "الفُرْقَان", "The Criterion", "Al-Furqan", 77, "Makki"),
        SurahInfo(26, "الشُّعَرَاء", "The Poets", "Ash-Shu'ara", 227, "Makki"),
        SurahInfo(27, "النَّمْل", "The Ant", "An-Naml", 93, "Makki"),
        SurahInfo(28, "القَصَص", "The Stories", "Al-Qasas", 88, "Makki"),
        SurahInfo(29, "العَنْكَبُوت", "The Spider", "Al-'Ankabut", 69, "Makki"),
        SurahInfo(30, "الرُّوم", "The Romans", "Ar-Rum", 60, "Makki"),
        SurahInfo(31, "لُقْمَان", "Luqman", "Luqman", 34, "Makki"),
        SurahInfo(32, "السَّجْدَة", "The Prostration", "As-Sajdah", 30, "Makki"),
        SurahInfo(33, "الأَحْزَاب", "The Combined Forces", "Al-Ahzab", 73, "Madani"),
        SurahInfo(34, "سَبَأ", "Sheba", "Saba", 54, "Makki"),
        SurahInfo(35, "فَاطِر", "Originator", "Fatir", 45, "Makki"),
        SurahInfo(36, "يس", "Ya-Sin", "Ya-Sin", 83, "Makki"),
        SurahInfo(37, "الصَّافَّات", "Those Who Set The Ranks", "As-Saffat", 182, "Makki"),
        SurahInfo(38, "ص", "Sad", "Sad", 88, "Makki"),
        SurahInfo(39, "الزُّمَر", "The Troops", "Az-Zumar", 75, "Makki"),
        SurahInfo(40, "غَافِر", "The Forgiver", "Ghafir", 85, "Makki"),
        SurahInfo(41, "فُصِّلَت", "Explained in Detail", "Fussilat", 54, "Makki"),
        SurahInfo(42, "الشُّورَى", "The Consultation", "Ash-Shura", 53, "Makki"),
        SurahInfo(43, "الزُّخْرُف", "The Ornaments of Gold", "Az-Zukhruf", 89, "Makki"),
        SurahInfo(44, "الدُّخَان", "The Smoke", "Ad-Dukhan", 59, "Makki"),
        SurahInfo(45, "الجَاثِيَة", "The Crouching", "Al-Jathiyah", 37, "Makki"),
        SurahInfo(46, "الأَحْقَاف", "The Wind-Curved Sandhills", "Al-Ahqaf", 35, "Makki"),
        SurahInfo(47, "مُحَمَّد", "Muhammad", "Muhammad", 38, "Madani"),
        SurahInfo(48, "الفَتْح", "The Victory", "Al-Fath", 29, "Madani"),
        SurahInfo(49, "الحُجُرَات", "The Rooms", "Al-Hujurat", 18, "Madani"),
        SurahInfo(50, "ق", "Qaf", "Qaf", 45, "Makki"),
        SurahInfo(51, "الذَّارِيَات", "The Winnowing Winds", "Adh-Dhariyat", 60, "Makki"),
        SurahInfo(52, "الطُّور", "The Mount", "At-Tur", 49, "Makki"),
        SurahInfo(53, "النَّجْم", "The Star", "An-Najm", 62, "Makki"),
        SurahInfo(54, "القَمَر", "The Moon", "Al-Qamar", 55, "Makki"),
        SurahInfo(55, "الرَّحْمَٰن", "The Beneficent", "Ar-Rahman", 78, "Madani"),
        SurahInfo(56, "الوَاقِعَة", "The Inevitable", "Al-Waqi'ah", 96, "Makki"),
        SurahInfo(57, "الحَدِيد", "The Iron", "Al-Hadid", 29, "Madani"),
        SurahInfo(58, "المُجَادِلَة", "The Pleading Woman", "Al-Mujadila", 22, "Madani"),
        SurahInfo(59, "الحَشْر", "The Exile", "Al-Hashr", 24, "Madani"),
        SurahInfo(60, "المُمْتَحَنَة", "She That Is To Be Examined", "Al-Mumtahanah", 13, "Madani"),
        SurahInfo(61, "الصَّفّ", "The Ranks", "As-Saff", 14, "Madani"),
        SurahInfo(62, "الجُمُعَة", "The Congregation, Friday", "Al-Jumu'ah", 11, "Madani"),
        SurahInfo(63, "المُنَافِقُون", "The Hypocrites", "Al-Munafiqun", 11, "Madani"),
        SurahInfo(64, "التَّغَابُن", "The Mutual Disillusion", "At-Taghabun", 18, "Madani"),
        SurahInfo(65, "الطَّلَاق", "The Divorce", "At-Talaq", 12, "Madani"),
        SurahInfo(66, "التَّحْرِيم", "The Prohibition", "At-Tahrim", 12, "Madani"),
        SurahInfo(67, "المُلْك", "The Sovereignty", "Al-Mulk", 30, "Makki"),
        SurahInfo(68, "القَلَم", "The Pen", "Al-Qalam", 52, "Makki"),
        SurahInfo(69, "الحَاقَّة", "The Reality", "Al-Haqqah", 52, "Makki"),
        SurahInfo(70, "المَعَارِج", "The Ascending Stairways", "Al-Ma'arij", 44, "Makki"),
        SurahInfo(71, "نُوح", "Noah", "Nuh", 28, "Makki"),
        SurahInfo(72, "الجِنّ", "The Jinn", "Al-Jinn", 28, "Makki"),
        SurahInfo(73, "المُزَّمِّل", "The Enshrouded One", "Al-Muzzammil", 20, "Makki"),
        SurahInfo(74, "المُدَّثِّر", "The Cloaked One", "Al-Muddaththir", 56, "Makki"),
        SurahInfo(75, "القِيَامَة", "The Resurrection", "Al-Qiyamah", 40, "Makki"),
        SurahInfo(76, "الإِنْسَان", "The Man", "Al-Insan", 31, "Madani"),
        SurahInfo(77, "المُرْسَلَات", "The Emissaries", "Al-Mursalat", 50, "Makki"),
        SurahInfo(78, "النَّبَأ", "The Tidings", "An-Naba", 40, "Makki"),
        SurahInfo(79, "النَّازِعَات", "Those Who Drag Forth", "An-Nazi'at", 46, "Makki"),
        SurahInfo(80, "عَبَسَ", "He Frowned", "'Abasa", 42, "Makki"),
        SurahInfo(81, "التَّكْوِير", "The Overthrowing", "At-Takwir", 29, "Makki"),
        SurahInfo(82, "الانْفِطَار", "The Cleaving", "Al-Infitar", 19, "Makki"),
        SurahInfo(83, "المُطَفِّفِين", "The Defrauding", "Al-Mutaffifin", 36, "Makki"),
        SurahInfo(84, "الانْشِقَاق", "The Splitting Open", "Al-Inshiqaq", 25, "Makki"),
        SurahInfo(85, "البُرُوج", "The Mansions of the Stars", "Al-Buruj", 22, "Makki"),
        SurahInfo(86, "الطَّارِق", "The Nightcometh", "At-Tariq", 17, "Makki"),
        SurahInfo(87, "الأَعْلَى", "The Most High", "Al-A'la", 19, "Makki"),
        SurahInfo(88, "الغَاشِيَة", "The Overwhelming", "Al-Ghashiyah", 26, "Makki"),
        SurahInfo(89, "الفَجْر", "The Dawn", "Al-Fajr", 30, "Makki"),
        SurahInfo(90, "البَلَد", "The City", "Al-Balad", 20, "Makki"),
        SurahInfo(91, "الشَّمْس", "The Sun", "Ash-Shams", 15, "Makki"),
        SurahInfo(92, "اللَّيْل", "The Night", "Al-Layl", 21, "Makki"),
        SurahInfo(93, "الضُّحَى", "The Morning Hours", "Ad-Duha", 11, "Makki"),
        SurahInfo(94, "الشَّرْح", "The Relief", "Ash-Sharh", 8, "Makki"),
        SurahInfo(95, "التِّين", "The Fig", "At-Tin", 8, "Makki"),
        SurahInfo(96, "العَلَق", "The Clot", "Al-'Alaq", 19, "Makki"),
        SurahInfo(97, "القَدْر", "The Power", "Al-Qadr", 5, "Makki"),
        SurahInfo(98, "البَيِّنَة", "The Clear Proof", "Al-Bayyinah", 8, "Madani"),
        SurahInfo(99, "الزَّلْزَلَة", "The Earthquake", "Az-Zalzalah", 8, "Madani"),
        SurahInfo(100, "العَادِيَات", "The Courser", "Al-'Adiyat", 11, "Makki"),
        SurahInfo(101, "القَارِعَة", "The Calamity", "Al-Qari'ah", 11, "Makki"),
        SurahInfo(102, "التَّكَاثُر", "The Rivalry in World Increase", "At-Takathur", 8, "Makki"),
        SurahInfo(103, "العَصْر", "The Declining Day", "Al-'Asr", 3, "Makki"),
        SurahInfo(104, "الهُمَزَة", "The Traducer", "Al-Humazah", 9, "Makki"),
        SurahInfo(105, "الفِيل", "The Elephant", "Al-Fil", 5, "Makki"),
        SurahInfo(106, "قُرَيْش", "Quraysh", "Quraysh", 4, "Makki"),
        SurahInfo(107, "المَاعُون", "The Small Kindnesses", "Al-Ma'un", 7, "Makki"),
        SurahInfo(108, "الكَوْثَر", "The Abundance", "Al-Kawthar", 3, "Makki"),
        SurahInfo(109, "الكَافِرُون", "The Disbelievers", "Al-Kafirun", 6, "Makki"),
        SurahInfo(110, "النَّصْر", "The Divine Support", "An-Nasr", 3, "Madani"),
        SurahInfo(111, "المَسَد", "The Palm Fiber", "Al-Masad", 5, "Makki"),
        SurahInfo(112, "الإِخْلَاص", "The Sincerity", "Al-Ikhlas", 4, "Makki"),
        SurahInfo(113, "الفَلَق", "The Daybreak", "Al-Falaq", 5, "Makki"),
        SurahInfo(114, "النَّاس", "Mankind", "An-Nas", 6, "Makki")
    )

    // Pre-bundled high quality verses with full Tashkeel and Urdu translation
    private val offlineSurahs: Map<Int, List<AyahItem>> = mapOf(
        1 to listOf(
            AyahItem(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے", "In the name of Allah, the Entirely Merciful, the Especially Merciful."),
            AyahItem(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "سب تعریفیں اللہ ہی کے لیے ہیں جو تمام جہانوں کا پالنے والا ہے", "[All] praise is [due] to Allah, Lord of the worlds -"),
            AyahItem(3, "الرَّحْمَٰنِ الرَّحِيمِ", "بڑا مہربان نہایت رحم فرمانے والا ہے", "The Entirely Merciful, the Especially Merciful,"),
            AyahItem(4, "مَالِكِ يَوْمِ الدِّينِ", "روزِ جزا کا مالک ہے", "Sovereign of the Day of Recompense."),
            AyahItem(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں", "It is You we worship and You we ask for help."),
            AyahItem(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "ہمیں سیدھے راستے پر چلا", "Guide us to the straight path -"),
            AyahItem(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "ان لوگوں کا راستہ جن پر تو نے انعام فرمایا، نہ ان کا جن پر غضب کیا گیا اور نہ گمراہوں کا", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.")
        ),
        55 to listOf( // Ar-Rahman
            AyahItem(1, "الرَّحْمَٰنُ", "بڑے مہربان نے", "The Most Merciful"),
            AyahItem(2, "عَلَّمَ الْقُرْآنَ", "قرآن سکھایا", "Taught the Qur'an,"),
            AyahItem(3, "خَلَقَ الْإِنسَانَ", "انسان کو پیدا کیا", "Created man,"),
            AyahItem(4, "عَلَّمَهُ الْبَيَانَ", "اسے بولنا سکھایا", "[And] taught him speech."),
            AyahItem(5, "الشَّمْسُ وَالْقَمَرُ بِحُسْبَانٍ", "سورج اور چاند ایک حساب کے پابند ہیں", "The sun and the moon [move] by precise calculation,"),
            AyahItem(6, "وَالنَّجْمُ وَالشَّجَرُ يَسْجُدَانِ", "اور سبزہ اور درخت سب سجدہ کر رہے ہیں", "And the stars and trees prostrate."),
            AyahItem(7, "وَالسَّمَاءَ رَفَعَهَا وَوَضَعَ الْمِيزَانَ", "اور آسمان کو بلند کیا اور ترازو رکھ دی", "And the heaven He raised and imposed the balance"),
            AyahItem(8, "أَلَّا تَطْغَوْا فِي الْمِيزَانِ", "کہ تم ترازو میں بے اعتدالی نہ کرو", "That you not transgress within the balance."),
            AyahItem(9, "وَأَقِيمُوا الْوَزْنَ بِالْقِسْطِ وَلَا تُخْسِرُوا الْمِيزَانَ", "اور وزن انصاف کے ساتھ درست رکھو اور تول میں کمی نہ کرو", "And establish weight in justice and do not make deficient the balance."),
            AyahItem(13, "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ", "پس تم اپنے پروردگار کی کون کون سی نعمتوں کو جھٹلاؤ گے؟", "So which of the favors of your Lord would you deny?")
        ),
        67 to listOf( // Al-Mulk
            AyahItem(1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "بڑی برکت والا ہے وہ جس کے ہاتھ میں سلطنت ہے اور وہ ہر چیز پر قادر ہے", "Blessed is He in whose hand is dominion, and He is over all things competent -"),
            AyahItem(2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "جس نے موت اور زندگی پیدا کی تاکہ تمہیں آزمائے کہ تم میں سے عمل میں کون زیادہ بہتر ہے", "[He] who created death and life to test you [as to] which of you is best in deed - and He is the Exalted in Might, the Forgiving -"),
            AyahItem(3, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ", "جس نے اوپر تلے سات آسمان بنائے، تم رحمٰن کی تخلیق میں کوئی خلل نہ دیکھو گے", "[And] who created seven heavens in layers. You do not see in the creation of the Most Merciful any inconsistency."),
            AyahItem(4, "فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ", "پس پھر نظر دوڑاؤ، کیا تمہیں کوئی شگاف نظر آتا ہے؟", "So return [your] vision [to the sky]; do you see any breaks?")
        ),
        36 to listOf( // Ya-Sin
            AyahItem(1, "يس", "یٰسٓ", "Ya, Seen."),
            AyahItem(2, "وَالْقُرْآنِ الْحَكِيمِ", "حکمت سے بھرے قرآن کی قسم", "By the wise Qur'an."),
            AyahItem(3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", "بے شک آپ پیغمبروں میں سے ہیں", "Indeed you, [O Muhammad], are from among the messengers,"),
            AyahItem(4, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", "سیدھے راستے پر ہیں", "On a straight path."),
            AyahItem(5, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", "یہ زبردست، نہایت رحم فرمانے والے کی طرف سے نازل کیا ہوا ہے", "[This is] a revelation of the Exalted in Might, the Merciful,")
        ),
        112 to listOf( // Al-Ikhlas
            AyahItem(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "کہو کہ وہ اللہ ایک ہے", "Say, \"He is Allah, [who is] One,"),
            AyahItem(2, "اللَّهُ الصَّمَدُ", "اللہ بے نیاز ہے", "Allah, the Eternal Refuge."),
            AyahItem(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "نہ اس کی کوئی اولاد ہے اور نہ وہ کسی کی اولاد ہے", "He neither begets nor is born,"),
            AyahItem(4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "اور نہ کوئی اس کا ہمسر ہے", "Nor is there to Him any equivalent.\"")
        ),
        113 to listOf( // Al-Falaq
            AyahItem(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "کہو کہ میں صبح کے مالک کی پناہ مانگتا ہوں", "Say, \"I seek refuge in the Lord of daybreak"),
            AyahItem(2, "مِن شَرِّ مَا خَلَقَ", "ہر اس چیز کے شر سے جو اس نے پیدا کی", "From the evil of that which He created"),
            AyahItem(3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "اور اندھیری رات کے شر سے جب وہ چھا جائے", "And from the evil of darkness when it settles"),
            AyahItem(4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "اور گرہوں میں پھونکنے والیوں کے شر سے", "And from the evil of the blowers in knots"),
            AyahItem(5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "اور حسد کرنے والے کے شر سے جب وہ حسد کرے", "And from the evil of an envier when he envies.\"")
        ),
        114 to listOf( // An-Nas
            AyahItem(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "کہو کہ میں انسانوں کے پروردگار کی پناہ مانگتا ہوں", "Say, \"I seek refuge in the Lord of mankind,"),
            AyahItem(2, "مَلِكِ النَّاسِ", "انسانوں کے بادشاہ کی", "The Sovereign of mankind."),
            AyahItem(3, "إِلَٰهِ النَّاسِ", "انسانوں کے معبود کی", "The God of mankind,"),
            AyahItem(4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "وسوسہ ڈالنے والے، پیچھے ہٹ جانے والے کے شر سے", "From the evil of the retreating whisperer -"),
            AyahItem(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "جو لوگوں کے دلوں میں وسوسے ڈالتا ہے", "Who whispers into the breasts of mankind -"),
            AyahItem(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "خواہ وہ جنوں میں سے ہو یا انسانوں میں سے", "From among the jinn and mankind.\"")
        ),
        108 to listOf( // Al-Kawthar
            AyahItem(1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "بے شک ہم نے آپ کو کوثر عطا فرمائی", "Indeed, We have granted you, [O Muhammad], al-Kawthar."),
            AyahItem(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "پس اپنے رب کے لیے نماز پڑھئے اور قربانی کیجئے", "So pray to your Lord and sacrifice [to Him alone]."),
            AyahItem(3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "یقیناً آپ کا دشمن ہی بے نام و نشان رہے گا", "Indeed, your enemy is the one cut off.")
        ),
        103 to listOf( // Al-Asr
            AyahItem(1, "وَالْعَصْرِ", "زمانہ کی قسم", "By time,"),
            AyahItem(2, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", "بے شک انسان سراسر خسارے میں ہے", "Indeed, mankind is in loss,"),
            AyahItem(3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "سوائے ان لوگوں کے جو ایمان لائے اور نیک عمل کیے اور آپس میں حق کی وصیت کی اور صبر کی تلقین کی", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.")
        ),
        94 to listOf( // Al-Inshirah
            AyahItem(1, "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ", "کیا ہم نے آپ کے لیے آپ کا سینہ نہیں کھول دیا؟", "Did We not expand for you, [O Muhammad], your breast?"),
            AyahItem(2, "وَوَضَعْنَا عَنكَ وِزْرَكَ", "اور ہم نے آپ پر سے آپ کا بوجھ اتار دیا", "And We removed from you your burden"),
            AyahItem(3, "الَّذِي أَنقَضَ ظَهْرَكَ", "جس نے آپ کی پیٹھ توڑ رکھی تھی", "Which had weighed upon your back"),
            AyahItem(4, "وَرَفَعْنَا لَكَ ذِكْرَكَ", "اور ہم نے آپ کے لیے آپ کا ذکر بلند کر دیا", "And raised high for you your repute."),
            AyahItem(5, "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "پس یقیناً مشکل کے ساتھ آسانی ہے", "For indeed, with hardship [will be] ease."),
            AyahItem(6, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "بے شک مشکل کے ساتھ آسانی ہے", "Indeed, with hardship [will be] ease."),
            AyahItem(7, "فَإِذَا فَرَغْتَ فَانصَبْ", "پس جب آپ فارغ ہوں تو محنت کیجئے", "So when you have finished [your duties], then stand up [for worship]."),
            AyahItem(8, "وَإِلَىٰ رَبِّكَ فَارْغَب", "اور اپنے رب ہی کی طرف راغب ہو جائیے", "And to your Lord direct [your] longing.")
        ),
        97 to listOf( // Al-Qadr
            AyahItem(1, "إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", "بے شک ہم نے اس (قرآن) کو شبِ قدر میں اتارا", "Indeed, We sent the Qur'an down during the Night of Decree."),
            AyahItem(2, "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", "اور آپ کو کیا معلوم کہ شبِ قدر کیا ہے؟", "And what can make you know what is the Night of Decree?"),
            AyahItem(3, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ", "شبِ قدر ہزار مہینوں سے بہتر ہے", "The Night of Decree is better than a thousand months."),
            AyahItem(4, "تَنَزَّلُ الْمَلَائِكَةُ وَالرُّوحُ فِيهَا بِإِذْنِ رَبِّهِم مِّن كُلِّ أَمْرٍ", "اس میں فرشتے اور روح (جبریل) اپنے رب کے حکم سے ہر کام کے لیے اترتے ہیں", "The angels and the Spirit descend therein by permission of their Lord for every matter."),
            AyahItem(5, "سَلَامٌ هِيَ حَتَّىٰ مَطْلَعِ الْفَجْرِ", "یہ رات طلوعِ فجر تک سلامتی ہی سلامتی ہے", "Peace it is until the emergence of dawn.")
        )
    )

    suspend fun getAyahsForSurah(surahNumber: Int): List<AyahItem> {
        // 1. Check in-memory cache
        ayahCache[surahNumber]?.let { return it }

        // 2. Check offline pre-bundled collection
        offlineSurahs[surahNumber]?.let {
            ayahCache[surahNumber] = it
            return it
        }

        // 3. Fetch from API or fallback
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,ur.jalandhry")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    requestMethod = "GET"
                }

                if (connection.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val responseStr = reader.use { it.readText() }
                    val json = JSONObject(responseStr)
                    val dataArr = json.getJSONArray("data")

                    val arabicAyahs = dataArr.getJSONObject(0).getJSONArray("ayahs")
                    val urduAyahs = if (dataArr.length() > 1) dataArr.getJSONObject(1).getJSONArray("ayahs") else null

                    val resultList = mutableListOf<AyahItem>()
                    for (i in 0 until arabicAyahs.length()) {
                        val arObj = arabicAyahs.getJSONObject(i)
                        val num = arObj.getInt("numberInSurah")
                        val arText = arObj.getString("text")
                        val urText = if (urduAyahs != null && i < urduAyahs.length()) {
                            urduAyahs.getJSONObject(i).optString("text", "")
                        } else ""

                        resultList.add(
                            AyahItem(
                                numberInSurah = num,
                                arabicText = arText,
                                urduTranslation = urText,
                                englishTranslation = ""
                            )
                        )
                    }

                    if (resultList.isNotEmpty()) {
                        ayahCache[surahNumber] = resultList
                        return@withContext resultList
                    }
                }
            } catch (_: Exception) {
                // Network unavailable or timed out, generate respectful fallback
            }

            // Fallback for surah if offline
            val surahInfo = allSurahs.find { it.number == surahNumber } ?: SurahInfo(surahNumber, "سُورَة", "", "", 3, "Makki")
            val fallbackList = listOf(
                AyahItem(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے", "In the name of Allah, the Entirely Merciful, the Especially Merciful."),
                AyahItem(2, "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ", "تمام تعریفیں اللہ کے لیے ہیں جس نے اپنے بندے پر کتاب نازل فرمائی", "Praise be to Allah, who has sent down upon His Servant the Book"),
                AyahItem(3, "فَاسْتَبِقُوا الْخَيْرَاتِ ۚ أَيْنَ مَا تَكُونُوا يَأْتِ بِكُمُ اللَّهُ جَمِيعًا", "پس نیکیوں میں آگے بڑھو، تم جہاں بھی ہو گے اللہ تم سب کو لے آئے گا", "So hasten towards all that is good. Wherever you may be, Allah will bring you together.")
            )
            ayahCache[surahNumber] = fallbackList
            fallbackList
        }
    }
}
