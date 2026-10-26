package com.abuwaja.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class AbuWajaIME : InputMethodService() {

    private enum class Mode { AR, AR_SHIFT, EN, EN_SHIFT, SYM, EMOJI, PHRASES }

    private var mode = Mode.AR
    private var lang = Mode.AR
    private var emojiCat = 0
    private var phraseTab = 0
    private lateinit var root: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private val sugViews = mutableListOf<TextView>()
    private var learned = mutableListOf<String>()

    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private val GOLD = 0xFFFFD700.toInt()
    private val DARK_GOLD = 0xFFB8860B.toInt()
    private val BG = 0xFF1A1A1A.toInt()
    private val BAR = 0xFF121212.toInt()
    private val KEY = 0xFF2B2B2B.toInt()
    private val SPECIAL = 0xFF3B3010.toInt()
    private val PRESSED = 0xFF6B520A.toInt()

    // ---------- الحروف ----------
    private val ar1 = listOf("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج","د")
    private val ar2 = listOf("ش","س","ي","ب","ل","ا","ت","ن","م","ك","ط","ذ")
    private val ar3 = listOf("ئ","ء","ؤ","ر","لا","ى","ة","و","ز","ظ")
    private val arS1 = listOf("\u064E","\u064B","\u064F","\u064C","\u0650","\u064D","\u0651","\u0652","\u0640","أ","إ","آ")
    private val arS2 = listOf("؟","،","؛","!",":","\"","(",")","-","/","«","»")
    private val en1 = listOf("q","w","e","r","t","y","u","i","o","p")
    private val en2 = listOf("a","s","d","f","g","h","j","k","l")
    private val en3 = listOf("z","x","c","v","b","n","m")
    private val sym1 = listOf("1","2","3","4","5","6","7","8","9","0")
    private val sym2 = listOf("١","٢","٣","٤","٥","٦","٧","٨","٩","٠")
    private val sym3 = listOf("،",".","؟","!",":","؛","-","/","(",")","@","#")

    // ---------- الإيموجي ----------
    private val emojiTabs = listOf("😀", "❤", "👍", "🌹", "☕")
    private val emojis = listOf(
        "😀 😃 😄 😁 😆 😅 😂 🤣 😊 😇 🙂 😉 😍 🥰 😘 😋 😜 🤪 😎 🤩 🥳 😏 😒 😞 😔 😟 😕 🙁 😣 😖 😫 😩 🥺 😢 😭 😤 😠 😡 🤬 🤯 😳 🥵 🥶 😱 😨 😰 😥 😓 🤗 🤔 🤭 🤫 😶 😐 🙄 😬 😴 🤤 😷 🤒 🤕 🤢 🤮 🥴 😵 🤠 😈 👿 💀 🤡 👻 👽 🤖 💩",
        "❤ 🧡 💛 💚 💙 💜 🖤 🤍 🤎 💔 💕 💞 💓 💗 💖 💘 💝 💟 ✨ ⭐ 🌟 💫 🔥 💥 💯 ✅ ❌ ⚠ ❓ ❗ 💢 💤 🎉 🎊 🎁 🏆 🥇 👑 💎 🔔 📌 🔒 🔑 ⚡ ☀ 🌙 ☁",
        "👍 👎 👌 ✌ 🤞 🤟 🤘 🤙 👈 👉 👆 👇 ☝ ✋ 🤚 🖐 🖖 👋 🤝 👏 🙌 👐 🤲 🙏 ✍ 💪 👀 👅 👄 🧠 🫶 🫡",
        "🌹 🌸 🌺 🌻 🌼 🌷 🌱 🌴 🌵 🍀 🍁 🍂 🌍 🌈 ⛅ 🌧 ❄ 💧 🌊 🐎 🐪 🦅 🦁 🐯 🐺 🐴 🐑 🐐 🐓 🐱 🐶 🦋 🐝 🕌 🕋",
        "☕ 🍵 🥛 🍞 🥐 🍳 🧀 🍗 🍖 🥩 🍔 🍟 🍕 🌯 🥙 🍚 🍛 🍲 🥗 🍎 🍌 🍇 🍉 🍓 🍊 🍋 🍍 🥭 🍒 🍰 🎂 🍩 🍪 🍫 🍬 🍯 🥤 🧃"
    )

    // ---------- الزخارف ----------
    private val decorStyles = listOf(
        "✦ %s ✦", "『 %s 』", "﴿ %s ﴾", "•°•✿ %s ✿•°•", "★彡 %s 彡★",
        "░▒▓ %s ▓▒░", "━━━ ❖ %s ❖ ━━━", "【 %s 】", "꧁ %s ꧂", "❝ %s ❞"
    )
    private val defaultPhrases = listOf(
        "أبو وجع الصبيحي",
        "✦ أبو وجع الصبيحي ✦",
        "『 أبو وجع الصبيحي 』",
        "السلام عليكم ورحمة الله وبركاته",
        "حياك الله يا هلا والله",
        "﷽",
        "━━━ ❖ ━━━"
    )

    // ---------- القاموس والتصحيح ----------
    private val arWords = ("السلام عليكم ورحمة الله وبركاته وعليكم السلام مرحباً أهلاً وسهلاً شكراً جزاك خيراً " +
        "يعطيك العافية يا هلا كيف حالك الحمد لله بخير إن شاء ما شاء بارك فيك صباح الخير مساء تصبح على " +
        "يسلمك يحفظك يرحمه ممتاز رائع جميل حبيبي أخي أختي صديقي الصبيحي أبو وجع يعني طيب تمام أكيد ليش وين متى " +
        "كيف ماذا لماذا هذا هذه ذلك الآن اليوم غداً أمس الليل النهار البيت الشغل الجوال رسالة اتصال عفواً آسف " +
        "معذرة مبروك عيدك مبارك رمضان كريم الجمعة السبت الأحد الاثنين الثلاثاء الأربعاء الخميس أنا أنت هو هي نحن " +
        "أنتم هم لكن لأن إذا إلى عن مع من في كل بعد قبل بين عند أيضاً جداً كثير قليل أكثر أقل أحسن أفضل " +
        "الأمر الوقت الناس الحياة الدنيا الأهل الأصحاب القلب العين الروح الحب الفرح الحزن الصبر الأمل").split(" ")

    private val enWords = ("the be to of and a in that have it for not on with he as you do at this but his by from " +
        "they we say her she or an will my one all would there their what so up out if about who get which go me " +
        "when make can like time no just him know take people into year your good some could them see other than " +
        "then now look only come its over think also back after use two how our work first well way even new want " +
        "because any these give day most us hello thanks thank please sorry welcome love friend morning night " +
        "today tomorrow yes okay").split(" ")

    private val corrections = mapOf(
        "اللة" to "الله", "هاذا" to "هذا", "هاذه" to "هذه", "لاكن" to "لكن",
        "انشالله" to "إن شاء الله", "ماشالله" to "ما شاء الله",
        "شكرا" to "شكراً", "مرحبا" to "مرحباً", "اهلا" to "أهلاً", "جدا" to "جداً",
        "ايضا" to "أيضاً", "انت" to "أنت", "انتم" to "أنتم", "انا" to "أنا",
        "اذا" to "إذا", "الى" to "إلى", "اخي" to "أخي", "اختي" to "أختي",
        "امي" to "أمي", "ابي" to "أبي", "اكثر" to "أكثر", "اكيد" to "أكيد",
        "احسن" to "أحسن", "افضل" to "أفضل", "الان" to "الآن", "لاني" to "لأني",
        "لانه" to "لأنه", "لان" to "لأن", "اسف" to "آسف", "غدا" to "غداً", "عفوا" to "عفواً",
        "i" to "I", "im" to "I'm", "dont" to "don't", "cant" to "can't", "wont" to "won't",
        "didnt" to "didn't", "doesnt" to "doesn't", "isnt" to "isn't", "ive" to "I've",
        "teh" to "the", "recieve" to "receive", "definately" to "definitely"
    )

    private val prefs by lazy { getSharedPreferences("abu", Context.MODE_PRIVATE) }

    // ================= دورة الحياة =================
    override fun onCreate() {
        super.onCreate()
        learned = prefs.getStringSet("learned", emptySet())!!.toMutableList()
    }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
            setPadding(dp(3), dp(2), dp(3), dp(2))
        }
        render()
        return root
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        val cls = info.inputType and InputType.TYPE_MASK_CLASS
        mode = if (cls == InputType.TYPE_CLASS_NUMBER || cls == InputType.TYPE_CLASS_PHONE) Mode.SYM else lang
        if (::root.isInitialized) render()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        if (::root.isInitialized) updateSuggestions()
    }

    // ================= الرسم =================
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun render() {
        root.removeAllViews()
        sugViews.clear()
        val rtl = when (mode) {
            Mode.EN, Mode.EN_SHIFT -> false
            Mode.EMOJI, Mode.PHRASES -> lang == Mode.AR
            else -> true
        }
        when (mode) {
            Mode.EMOJI -> {
                root.addView(tabBar(emojiTabs, emojiCat, null) { emojiCat = it; render() })
                root.addView(emojiPanel())
            }
            Mode.PHRASES -> {
                root.addView(tabBar(listOf("عبارات", "زخارف"), phraseTab, Pair("＋ حفظ") { savePhrase() }) {
                    phraseTab = it; render()
                })
                root.addView(phrasePanel())
            }
            else -> {
                root.addView(suggestionBar(rtl))
                keyRows(rtl).forEach { root.addView(it) }
            }
        }
        root.addView(bottomRow(rtl))
        updateSuggestions()
    }

    private fun keyBg(fill: Int): StateListDrawable {
        fun shape(c: Int) = GradientDrawable().apply {
            setColor(c); cornerRadius = dp(8).toFloat(); setStroke(dp(1), DARK_GOLD)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), shape(PRESSED))
            addState(intArrayOf(), shape(fill))
        }
    }

    private fun key(
        label: String, weight: Float, special: Boolean = false, size: Float = 18f,
        repeat: Boolean = false, action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = size
            gravity = Gravity.CENTER
            setTextColor(GOLD)
            background = keyBg(if (special) SPECIAL else KEY)
            layoutParams = LinearLayout.LayoutParams(0, MATCH, weight).apply {
                setMargins(dp(2), dp(3), dp(2), dp(3))
            }
            val rep = object : Runnable {
                override fun run() { action(); handler.postDelayed(this, 50) }
            }
            setOnTouchListener { v, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.isPressed = true
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        action()
                        if (repeat) handler.postDelayed(rep, 400)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.isPressed = false
                        handler.removeCallbacks(rep)
                    }
                }
                true
            }
        }
    }

    private fun rowOf(rtl: Boolean, views: List<View>, h: Int = 48): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH, dp(h))
            (if (rtl) views.reversed() else views).forEach { addView(it) }
        }

    private fun spacer(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(0, MATCH, 0.5f)
    }

    private fun label(s: String) =
        if (s.length == 1 && s[0].code in 0x064B..0x0652) "\u25CC" + s else s

    private fun letterKeys(items: List<String>): List<View> =
        items.map { s -> key(label(s), 1f) { typeText(s) } }

    private fun keyRows(rtl: Boolean): List<View> {
        val shiftOn = mode == Mode.AR_SHIFT || mode == Mode.EN_SHIFT
        val shift = key(if (shiftOn) "⬆" else "⇧", 1.5f, true) { toggleShift() }
        return when (mode) {
            Mode.AR, Mode.AR_SHIFT -> listOf(
                rowOf(rtl, letterKeys(if (shiftOn) arS1 else ar1)),
                rowOf(rtl, letterKeys(if (shiftOn) arS2 else ar2)),
                rowOf(rtl, listOf<View>(shift) + letterKeys(ar3))
            )
            Mode.EN, Mode.EN_SHIFT -> {
                val c = { s: String -> if (shiftOn) s.uppercase() else s }
                listOf(
                    rowOf(rtl, letterKeys(en1.map(c))),
                    rowOf(rtl, listOf(spacer()) + letterKeys(en2.map(c)) + listOf(spacer())),
                    rowOf(rtl, listOf<View>(shift) + letterKeys((en3 + listOf(",", ".")).map(c)))
                )
            }
            else -> listOf(
                rowOf(rtl, letterKeys(sym1)),
                rowOf(rtl, letterKeys(sym2)),
                rowOf(rtl, letterKeys(sym3))
            )
        }
    }

    private fun bottomRow(rtl: Boolean): View {
        val inPanel = mode == Mode.SYM || mode == Mode.EMOJI || mode == Mode.PHRASES
        val ar = lang == Mode.AR
        val v = listOf<View>(
            key(if (inPanel) (if (ar) "أب" else "ABC") else "123", 1.2f, true, 14f) {
                mode = if (inPanel) lang else Mode.SYM; render()
            },
            key(if (ar) "EN" else "ع", 1f, true, 14f) {
                lang = if (ar) Mode.EN else Mode.AR; mode = lang; render()
            },
            key("😊", 1f, true, 18f) { mode = if (mode == Mode.EMOJI) lang else Mode.EMOJI; render() },
            key("✦", 1f, true, 18f) { mode = if (mode == Mode.PHRASES) lang else Mode.PHRASES; render() },
            key(if (ar) "أبو وجع الصبيحي" else "Abu Waja", 3.2f, false, 13f) { onSpace() },
            key(if (ar) "،" else ",", 0.9f) { typeText(if (ar) "،" else ",") },
            key("⌫", 1.2f, true, 18f, true) { sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL); updateSuggestions() },
            key("↵", 1.2f, true, 18f) { onEnter() }
        )
        return rowOf(rtl, v)
    }

    // ================= الاقتراحات =================
    private fun suggestionBar(rtl: Boolean): View {
        sugViews.clear()
        repeat(3) {
            sugViews.add(TextView(this).apply {
                textSize = 16f
                gravity = Gravity.CENTER
                maxLines = 1
                setTextColor(GOLD)
                layoutParams = LinearLayout.LayoutParams(0, MATCH, 1f)
                setOnClickListener { onSuggestion((it as TextView).text.toString()) }
            })
        }
        return rowOf(rtl, sugViews.toList(), 40).apply { setBackgroundColor(BAR) }
    }

    private fun isWordChar(c: Char) =
        c.isLetter() || Character.getType(c) == Character.NON_SPACING_MARK.toInt()

    private fun currentWord(): String {
        val t = currentInputConnection?.getTextBeforeCursor(40, 0)?.toString() ?: return ""
        var i = t.length
        while (i > 0 && isWordChar(t[i - 1])) i--
        return t.substring(i)
    }

    private fun norm(s: String) = s.lowercase()
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
        .replace('ى', 'ي').replace('ة', 'ه')

    private fun candidates(w: String): List<String> {
        val out = LinkedHashSet<String>()
        (corrections[w] ?: corrections[w.lowercase()])?.let { out.add(it) }
        val n = norm(w)
        (learned + arWords + enWords)
            .filter { it != w && norm(it).startsWith(n) }
            .sortedBy { it.length }
            .forEach { if (out.size < 3) out.add(it) }
        return out.toList()
    }

    private fun updateSuggestions() {
        if (sugViews.size < 3) return
        val w = currentWord()
        if (w.isEmpty()) {
            sugViews[0].text = ""
            sugViews[1].text = "✦ أبو وجع الصبيحي ✦"
            sugViews[2].text = ""
        } else {
            val c = candidates(w)
            for (i in 0..2) sugViews[i].text = c.getOrNull(i) ?: ""
        }
    }

    private fun onSuggestion(s: String) {
        if (s.isEmpty()) return
        val ic = currentInputConnection ?: return
        val w = currentWord()
        if (w.isEmpty()) {
            ic.commitText(s, 1)
        } else {
            ic.deleteSurroundingText(w.length, 0)
            ic.commitText("$s ", 1)
            learn(s)
        }
        updateSuggestions()
    }

    private fun learn(w: String) {
        if (w.length < 3 || w in learned || w in arWords || w in enWords) return
        learned.add(0, w)
        if (learned.size > 300) learned = learned.take(300).toMutableList()
        prefs.edit().putStringSet("learned", learned.toSet()).apply()
    }

    // ================= الكتابة =================
    private fun typeText(s: String) {
        currentInputConnection?.commitText(s, 1)
        if (mode == Mode.EN_SHIFT) { mode = Mode.EN; render() } else updateSuggestions()
    }

    private fun toggleShift() {
        mode = when (mode) {
            Mode.AR -> Mode.AR_SHIFT
            Mode.AR_SHIFT -> Mode.AR
            Mode.EN -> Mode.EN_SHIFT
            Mode.EN_SHIFT -> Mode.EN
            else -> mode
        }
        render()
    }

    private fun onSpace() {
        val ic = currentInputConnection ?: return
        val w = currentWord()
        if (w.isNotEmpty()) {
            val fix = corrections[w] ?: corrections[w.lowercase()]
            if (fix != null) {
                ic.deleteSurroundingText(w.length, 0)
                ic.commitText(fix, 1)
            } else learn(w)
        }
        ic.commitText(" ", 1)
        updateSuggestions()
    }

    private fun onEnter() {
        val ei = currentInputEditorInfo
        val action = ei?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        val noAction = ((ei?.imeOptions ?: 0) and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (!noAction && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            currentInputConnection?.performEditorAction(action)
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    // ================= الألواح =================
    private fun tab(label: String, sel: Boolean, click: () -> Unit) = TextView(this).apply {
        text = label
        textSize = 15f
        gravity = Gravity.CENTER
        setTextColor(if (sel) Color.BLACK else GOLD)
        setBackgroundColor(if (sel) GOLD else Color.TRANSPARENT)
        layoutParams = LinearLayout.LayoutParams(0, MATCH, 1f)
        setOnClickListener { click() }
    }

    private fun tabBar(
        labels: List<String>, selected: Int, extra: Pair<String, () -> Unit>?, onTab: (Int) -> Unit
    ): View {
        val views = labels.mapIndexed { i, l -> tab(l, i == selected) { onTab(i) } }.toMutableList()
        if (extra != null) views.add(tab(extra.first, false) { extra.second() })
        return rowOf(lang == Mode.AR, views, 40).apply { setBackgroundColor(BAR) }
    }

    private fun emojiPanel(): View {
        val list = emojis[emojiCat].split(" ").filter { it.isNotBlank() }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        list.chunked(8).forEach { chunk ->
            val r = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH, dp(46))
            }
            for (j in 0 until 8) {
                val e = chunk.getOrNull(j)
                r.addView(TextView(this).apply {
                    text = e ?: ""
                    textSize = 24f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(0, MATCH, 1f)
                    if (e != null) setOnClickListener { currentInputConnection?.commitText(e, 1) }
                })
            }
            col.addView(r)
        }
        return ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH, dp(144))
            addView(col)
        }
    }

    private fun item(text: String, onClick: () -> Unit, onLong: (() -> Unit)? = null) =
        TextView(this).apply {
            this.text = text
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(GOLD)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = keyBg(KEY)
            layoutParams = LinearLayout.LayoutParams(MATCH, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(4), dp(3), dp(4), dp(3))
            }
            setOnClickListener { onClick() }
            if (onLong != null) setOnLongClickListener { onLong(); true }
        }

    private fun phrasePanel(): View {
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (phraseTab == 0) {
            val list = loadPhrases()
            list.forEach { p ->
                col.addView(item(p, { currentInputConnection?.commitText(p, 1) }, {
                    val l = loadPhrases(); l.remove(p); storePhrases(l)
                    toast("تم حذف العبارة"); render()
                }))
            }
            col.addView(TextView(this).apply {
                text = "اضغط مطولاً على أي عبارة لحذفها"
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(DARK_GOLD)
                setPadding(0, dp(6), 0, dp(6))
            })
        } else {
            col.addView(item("تطويل:  " + stretch("أبو وجع الصبيحي"), { applyDecor { stretch(it) } }))
            decorStyles.forEach { st ->
                col.addView(item(st.replace("%s", "أبو وجع الصبيحي"), {
                    applyDecor { st.replace("%s", it) }
                }))
            }
            col.addView(TextView(this).apply {
                text = "اكتب نصاً ثم اختر زخرفة، وإن كان الحقل فارغاً يُكتب اسمك"
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(DARK_GOLD)
                setPadding(dp(8), dp(6), dp(8), dp(6))
            })
        }
        return ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH, dp(144))
            addView(col)
        }
    }

    private fun loadPhrases(): MutableList<String> {
        val s = prefs.getString("phrases", null) ?: return defaultPhrases.toMutableList()
        return if (s.isEmpty()) mutableListOf() else s.split("\u0001").toMutableList()
    }

    private fun storePhrases(l: List<String>) {
        prefs.edit().putString("phrases", l.joinToString("\u0001")).apply()
    }

    private fun savePhrase() {
        val ic = currentInputConnection ?: return
        val b = ic.getTextBeforeCursor(2000, 0)?.toString() ?: ""
        val a = ic.getTextAfterCursor(2000, 0)?.toString() ?: ""
        val t = (b + a).trim()
        if (t.isEmpty()) { toast("اكتب النص أولاً ثم اضغط حفظ"); return }
        val l = loadPhrases()
        if (t !in l) l.add(0, t)
        storePhrases(l)
        toast("تم حفظ العبارة ✦")
        phraseTab = 0
        render()
    }

    private fun applyDecor(f: (String) -> String) {
        val ic = currentInputConnection ?: return
        val b = ic.getTextBeforeCursor(2000, 0)?.toString() ?: ""
        val a = ic.getTextAfterCursor(2000, 0)?.toString() ?: ""
        val t = (b + a).trim().ifEmpty { "أبو وجع الصبيحي" }
        ic.deleteSurroundingText(b.length, a.length)
        ic.commitText(f(t), 1)
    }

    private val nonConnect = "ادذرزوةىأإآؤء"

    private fun isAr(c: Char) = c in '\u0621'..'\u064A'

    private fun stretch(s: String): String {
        val sb = StringBuilder()
        for (i in s.indices) {
            val c = s[i]
            sb.append(c)
            if (i + 1 < s.length && isAr(c) && c !in nonConnect && isAr(s[i + 1])) sb.append('\u0640')
        }
        return sb.toString()
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
