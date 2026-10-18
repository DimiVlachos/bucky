# Πλάνο: πληκτρολόγιο τύπου PC για Android

Κωδικό όνομα repo: `TBD` (βλ. 8.1). Έκδοση πλάνου 1.0, 18 Οκτωβρίου 2026.
Βασίζεται στην έρευνα της ίδιας ημέρας για εγκαταλελειμμένα open-source mobile projects, όπου το Hacker's Keyboard (klausw/hackerskeyboard) βγήκε πρώτο σε ευκαιρία: 2.4k stars, 514 ανοιχτά issues, 26 ανοιχτά PRs, τελευταίο release Νοέμβριο 2018, εγκαταστάσεις 10M+ (από mirror, not verified στο Play), νέα issues ακόμη και το 2026.

## 0. Σύνοψη

Ξαναχτίζουμε από το μηδέν την ιδέα του Hacker's Keyboard: ένα soft keyboard για Android με πλήρη διάταξη υπολογιστή (σειρά αριθμών, Tab, Ctrl, Alt, Esc, F1 έως F12, βελάκια, Home/End/PgUp/PgDn), για όσους χρησιμοποιούν το κινητό ή το tablet ως τερματικό (Termux, SSH clients, έλεγχος coding agents), γράφουν κώδικα ή δουλεύουν σε tablet και foldable χωρίς φυσικό πληκτρολόγιο.

- Μοντέλο: open-core. Όλος ο κώδικας Apache-2.0. Δωρεάν app στο Play με Pro unlock (one-time IAP). Το `foss` flavor (GitHub Releases, IzzyOnDroid, F-Droid) έχει τα πάντα ξεκλείδωτα.
- Τεχνολογία: Kotlin, custom Canvas view για το ίδιο το πληκτρολόγιο, Jetpack Compose μόνο για settings και onboarding, Hilt, Coroutines, DataStore με kotlinx.serialization. Android-only.
- Χρόνος: 15 με 20 ώρες την εβδομάδα, περίπου 14 εβδομάδες μέχρι το v1.0 στο Play, με 2 εβδομάδες επαλήθευσης στην αρχή και 1 εβδομάδα buffer στο τέλος.
- Αρχή νούμερο ένα: το IME δεν κάνει ποτέ crash. Ένα πληκτρολόγιο που πέφτει αφήνει τον χρήστη χωρίς τρόπο να γράψει ούτε το bug report.
- Αρχή νούμερο δύο: καμία άδεια INTERNET. Είναι το ισχυρότερο επιχείρημα εμπιστοσύνης για ένα πληκτρολόγιο και κλείνει το θέμα Data Safety.

## 1. Στόχος, κοινό, θέση στην αγορά

### 1.1 Τι λύνουμε

Τα σύγχρονα πληκτρολόγια Android είναι φτιαγμένα για chat. Σε ένα τερματικό, σε vim keybindings μέσα σε web editor, σε spreadsheet ή σε remote desktop χρειάζεσαι Ctrl, Esc, Tab, βελάκια, F-keys και modifier chording, και τα χρειάζεσαι χωρίς να αλλάζεις layer κάθε δύο δευτερόλεπτα. Το Hacker's Keyboard το έκανε αυτό και πέθανε από αδράνεια του maintainer, όχι επειδή χάθηκε η ανάγκη.

### 1.2 Κοινό, σε σειρά προτεραιότητας

1. Χρήστες Termux, SSH clients (ConnectBot, JuiceSSH, Termius) και όσοι ελέγχουν coding agents ή homelab από το κινητό. Είναι το κοινό που ξέρει ακριβώς τι θέλει και πληρώνει για εργαλεία.
2. Developers και sysadmins που θέλουν Ctrl/Esc/Tab/βελάκια σε κάθε εφαρμογή.
3. Χρήστες tablet και foldable σε desktop windowing χωρίς φυσικό πληκτρολόγιο.
4. Παλιοί χρήστες του Hacker's Keyboard που ψάχνουν αντικαταστάτη (issues #875, #939, #997).

### 1.3 Θέση έναντι ανταγωνισμού

| Προϊόν | Τι κάνει καλά | Τι λείπει για το κοινό μας |
|---|---|---|
| Hacker's Keyboard | Το πρότυπο: πλήρης 5-row διάταξη | Νεκρό από το 2018, προβλήματα σε Android 14+, μπλοκάρεται από τράπεζες (#997) |
| Unexpected Keyboard | FOSS, αγαπητό σε Termux, swipe προς τις γωνίες για σύμβολα | Διαφορετικό μοντέλο αλληλεπίδρασης, όχι 1:1 με PC layout (βλ. HN 42376724) |
| HeliBoard, FlorisBoard | Σύγχρονα FOSS γενικής χρήσης | Έμφαση σε prediction και γλώσσες, όχι σε PC layout και modifier chording |
| FUTO Keyboard | Offline ML prediction | Δεν στοχεύει power users τερματικού |
| Gboard, SwiftKey | Prediction, swipe, οικοσύστημα | Καμία PC διάταξη, cloud, όχι εμπιστοσύνη για τερματικό |

Η θέση μας σε μία πρόταση: το μόνο σύγχρονο πληκτρολόγιο για Android που συμπεριφέρεται σαν πληκτρολόγιο υπολογιστή, ανοιχτού κώδικα, χωρίς πρόσβαση στο διαδίκτυο.

### 1.4 Τι δεν είμαστε

- Δεν ανταγωνιζόμαστε το Gboard σε prediction, swipe ή emoji. Το v1 δεν έχει καθόλου prediction, και αυτό είναι απόφαση, όχι παράλειψη: οι power users το απενεργοποιούν ούτως ή άλλως και είναι το 60% της δουλειάς ενός πληκτρολογίου για το 20% της αξίας εδώ.
- Όχι iOS. Τα custom keyboards στο iOS είναι υπερβολικά περιορισμένα για αυτή την ιδέα.
- Όχι clipboard manager, voice typing, themes marketplace στο v1.

## 2. Παραδοχές και περιορισμοί

- 15 με 20 ώρες την εβδομάδα, solo, παράλληλα με πλήρη εργασία. Συνέπεια: σταθερό εβδομαδιαίο cap, όχι crunch, και ρητό buffer.
- Android-only, Kotlin. Το IME API είναι Android-specific. Το KMP δεν προσφέρει τίποτα εδώ και προσθέτει build complexity.
- minSdk 26, targetSdk το εκάστοτε τελευταίο stable. Το 26 καλύπτει σχεδόν όλη την ενεργή βάση, δίνει adaptive icons και autofill APIs, και αποφεύγει branches για παλιότερα APIs.
- Καμία άδεια INTERNET. Άρα όχι analytics SDK, όχι crash reporting SDK. Μετρήσεις μόνο από Play Console (vitals, installs, IAP) και από την κοινότητα.
- Χωρίς backend. Κανένα feature δεν εξαρτάται από server.
- Το κοινό προτιμά FOSS και δεν δέχεται συνδρομή σε πληκτρολόγιο. Άρα πλήρως ανοιχτός κώδικας και one-time Pro.
- Ο κώδικας δεν είναι το moat. Με τα AI tools του 2026 ο κώδικας αντιγράφεται. Το moat είναι η ποιότητα της ενσωμάτωσης (latency, edge cases του InputConnection, συμπεριφορά ανά OEM), η εμπιστοσύνη και η παρουσία στα κανάλια του κοινού.

## 3. Scope ανά έκδοση

### 3.1 v0, εβδομάδες 1 με 2: επαλήθευση και spike

Παραδοτέα: λίστα κενών από καθημερινή χρήση Unexpected Keyboard και HeliBoard σε Termux, top-10 λίστα ζητούμενων features από τα issues #875 (42 σχόλια), #939 και Reddit, landing page με waitlist, τεχνικό spike για latency με Canvas view, απόφαση go/no-go (βλ. 10).

### 3.2 v1.0, το MVP

| Must | Should | Won't (v1) |
|---|---|---|
| Διατάξεις 4 και 5 σειρών, πλήρης PC (Esc, Tab, Ctrl, Alt, Fn, βελάκια, Home/End/PgUp/PgDn, Del, Ins) | Compact διάταξη για browsers και chat | Prediction, autocorrect, swipe |
| Modifiers: one-shot, locked, chording με multi-touch | Fn layer με F1 έως F12 και media keys | Clipboard history |
| Σωστή αποστολή KeyEvents με meta state (Ctrl+C, Esc, βελάκια) ώστε να δουλεύουν Termux, vim, SSH | Αυτόματη εναλλαγή σε compact σε password fields | Emoji keyboard |
| Layouts EN (US, UK), EL, Dvorak, Colemak, όλα data-driven | DE και FR layouts αν υπάρχει native tester | Voice input |
| Long-press alternates, key repeat, haptics | Αριθμητικό layout για inputType number/phone | Themes πέρα από Material You και dark |
| Built-in προφίλ για Termux, ConnectBot, JuiceSSH, Termius | Ρύθμιση ύψους και split mode σε πλάτος 600dp+ | Layout editor (v1.1, Pro) |
| Material You dynamic colors, light και dark | Προεπισκόπηση πλήκτρου μέσα στο view, όχι PopupWindow | Custom per-app προφίλ (v1.1, Pro) |
| Onboarding 3 βημάτων: enable, select, try | Backup ρυθμίσεων σε αρχείο (v1.1, Pro) | Physical keyboard remapping |
| TalkBack υποστήριξη με ExploreByTouchHelper | | |
| Flavors `play` και `foss`, Pro gating με Play Billing | | |
| Σελίδα Play, privacy policy, Data Safety, IzzyOnDroid και F-Droid submission | | |

### 3.3 v1.1, μήνες 4 με 5

Layout editor με απεριόριστα custom layouts (Pro), custom per-app προφίλ (Pro), themes με import/export (Pro), backup/restore ρυθμίσεων (Pro), macros/snippets key για συχνές εντολές τερματικού (Pro), Baseline Profile, high-contrast θέμα, επιπλέον γλώσσες από contributors.

### 3.4 v1.2 και μετά, μόνο αν περάσει το Gate 1

On-device prediction και autocorrect (Pro), mic key που εναλλάσσει στο voice IME του συστήματος, emoji μέσω alternates.

## 4. Αρχιτεκτονική και τεχνικές αποφάσεις

### 4.1 Modules

```
:app             Manifest, flavors play/foss, Hilt Application, το InputMethodService, wiring
:ime             Layout model και parser, KeyboardEngine, InputDispatcher, KeyboardView (Canvas), theming tokens
:settings        Compose: onboarding, settings, προφίλ ανά εφαρμογή, Pro screen, layout editor (v1.1)
:billing         Interfaces: ProEntitlement, BillingRepository, sealed BillingOutcome
:billing-play    Υλοποίηση με Play Billing Library
:billing-foss    Υλοποίηση που επιστρέφει πάντα Unlocked
```

Γιατί έξι και όχι περισσότερα: το `:ime` δεν εξαρτάται από Compose ούτε από billing, κρατά τον build χρόνο χαμηλό και εγγυάται ότι τίποτα βαρύ δεν μπαίνει στο IME window. Το billing χωρίζεται σε modules επειδή το `foss` flavor δεν πρέπει να περιέχει τη βιβλιοθήκη της Google (απαίτηση F-Droid), και ο καθαρότερος τρόπος είναι `playImplementation(project(":billing-play"))` και `fossImplementation(project(":billing-foss"))` στο `:app`. Convention plugins και build-logic: όχι στο v1. Με έξι modules η επανάληψη στα build scripts είναι ανεκτή, και η επένδυση σε build υποδομή πριν βγει το προϊόν είναι ακριβώς το μοτίβο που σκότωσε το Ivy Wallet. Κανένα νέο module χωρίς ενημέρωση αυτού του αρχείου.

Εξαίρεση εργαλείων build: το `:detekt-rules` (custom detekt rules από άλλα projects του Dimitris: NoNotNullAssertion, CatchBlockHygiene, TimberErrorArgument, HardcodedColor) είναι Kotlin/JVM module μόνο για static analysis, δεν μπαίνει στο APK και δεν μετράει στα έξι modules του προϊόντος.

### 4.2 Rendering: Canvas, όχι Compose, στο IME window

Το πληκτρολόγιο είναι ένα custom `View` που ζωγραφίζει σε Canvas. Το Compose μέσα σε `InputMethodService` απαιτεί χειροκίνητο wiring των `LifecycleOwner`, `ViewModelStoreOwner` και `SavedStateRegistryOwner` στο decor view του IME window και φέρνει κόστος recomposition σε κάθε key press. Σε πληκτρολόγιο ο προϋπολογισμός από το άγγιγμα μέχρι το glyph είναι ένα frame, και με Canvas το κόστος ανά frame είναι ντετερμινιστικό. Το Compose χρησιμοποιείται κανονικά στο Activity των ρυθμίσεων.

Η προεπισκόπηση πλήκτρου (το μεγεθυμένο γράμμα πάνω από το πλήκτρο) ζωγραφίζεται μέσα στο ίδιο View και όχι με `PopupWindow`: τα popups είναι ξεχωριστά windows με δικό τους latency και jank. Το View επεκτείνεται πάνω από τα πλήκτρα και με `onComputeInsets` δηλώνουμε ως touchable region μόνο την περιοχή των πλήκτρων.

### 4.3 Μοντέλο layout: data, όχι κώδικας

Κάθε διάταξη είναι ένα JSON αρχείο. Έτσι Dvorak, Colemak, Ελληνικά και οι μελλοντικές γλώσσες από contributors είναι data, και ο layout editor του v1.1 γράφει το ίδιο format.

```json
{
  "id": "pc_full_en_us",
  "name": "PC Full (US)",
  "rows": [
    {
      "keys": [
        { "key": "ESC" },
        { "chars": ["1", "!"], "longPress": ["F1"] },
        { "chars": ["2", "@"], "longPress": ["F2"] }
      ]
    },
    {
      "keys": [
        { "key": "TAB", "width": 1.5 },
        { "chars": ["q", "Q"], "longPress": ["1"] }
      ]
    }
  ]
}
```

Μοντέλο σε Kotlin: `sealed interface KeyDef` με `Char(primary, shifted, alternates)`, `Special(code: KeyCode)`, `Modifier(kind: ModifierKind)`, `LayoutSwitch(targetId)`. Πλάτος σε μονάδες, ύψος σειράς από τις ρυθμίσεις. Ο parser χρησιμοποιεί kotlinx.serialization και επιστρέφει `Result<Layout>` με δομημένα σφάλματα (διαδρομή στο JSON και αιτία). Τα built-in layouts είναι assets, τα custom του χρήστη ζουν σε `filesDir/layouts`.

### 4.4 Engine: από το άγγιγμα στο InputConnection

`KeyboardEngine` σε καθαρό Kotlin, χωρίς Android imports εκτός από `KeyEvent` σταθερές. Δέχεται pointer δεδομένα ως primitives, `onPointer(pointerId, x, y, action, timeMs, sink)`, τα αντιστοιχίζει σε πλήκτρο μέσω προϋπολογισμένης γεωμετρίας και εκπέμπει μέσω `InputSink` (`commitText(text)`, `sendKey(keyCode, metaState)`). Primitives και sink αντί για αντικείμενα `PointerEvent` και `InputCommand`, ώστε το `onTouchEvent` να μένει χωρίς allocations (βλ. 4.12 και 13). Οι εντολές που εκπέμπονται:

- `commitText(text)` για απλούς χαρακτήρες.
- `sendKey(keyCode, metaState)` για special keys και για οποιονδήποτε χαρακτήρα όταν είναι ενεργό Ctrl ή Alt.
- Backspace και Delete μέσω KeyEvents `DEL` και `FORWARD_DEL`, ώστε τα τερματικά να παίρνουν το πραγματικό πλήκτρο.
- Εναλλαγή layout και modifiers είναι εσωτερική κατάσταση του engine, όχι εντολή προς το `InputConnection`.

Ο `InputDispatcher` υλοποιεί το `InputSink` πάνω στο `InputConnection`: `commitText` για χαρακτήρες, ζεύγος `sendKeyEvent(down)` και `sendKeyEvent(up)` με `META_CTRL_ON | META_CTRL_LEFT_ON` (αντίστοιχα για Alt και Shift) για τα υπόλοιπα. Γιατί KeyEvents και όχι text: το Termux, ο vim και κάθε SSH client διαβάζουν raw key events για Ctrl+C, Esc και βελάκια. Το `commitText` δεν θα δούλευε ποτέ εκεί.

Modifiers, ένα state machine ανά modifier: `Off`, με tap γίνεται `OneShot`, με δεύτερο tap `Locked`, με τρίτο `Off`. Το `OneShot` καταναλώνεται στο επόμενο πλήκτρο. Chording: όταν ένας pointer κρατά modifier και άλλος pointer πατά πλήκτρο, ο modifier εφαρμόζεται μόνο σε εκείνο το πάτημα. Η ανίχνευση γίνεται με tracking ανά pointer id.

Key repeat για Backspace, Delete, βελάκια και space: αρχική καθυστέρηση 400 ms και μετά κάθε 50 ms, με coroutine στο `Dispatchers.Main.immediate` που ακυρώνεται σε `ACTION_UP` ή `ACTION_CANCEL`. Long-press στα 300 ms ανοίγει πλέγμα alternates, ολίσθηση επιλέγει, άφημα επιβεβαιώνει. Auto-capitalization κλειστό εξ ορισμού, με επιλογή που χρησιμοποιεί `InputConnection.getCursorCapsMode`.

### 4.5 InputMethodService: lifecycle, insets, EditorInfo

- `onCreate`: δημιουργεί `serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)`, ξεκινά τη συλλογή ρυθμίσεων από το DataStore και φορτώνει το τρέχον layout σε `Dispatchers.Default`. Το service δημιουργείται πριν εμφανιστεί το view, άρα στις περισσότερες περιπτώσεις το snapshot είναι έτοιμο στο `onCreateInputView`. Αν δεν είναι, το view ξεκινά με defaults και ενημερώνεται.
- `onCreateInputView`: επιστρέφει το `KeyboardView`, φθηνό, χωρίς I/O.
- `onStartInputView(info, restarting)`: διαβάζει `EditorInfo.packageName`, `inputType` και `imeOptions`, επιλέγει προφίλ και layout. Σε password variation απενεργοποιεί την προεπισκόπηση πλήκτρου. Σε number/phone inputType γυρίζει σε αριθμητικό layout αν το προφίλ δεν λέει αλλιώς.
- `onComputeInsets`: ορίζει `contentTopInsets` και `visibleTopInsets` ώστε το window να καλύπτει την περιοχή προεπισκόπησης αλλά η touchable region να είναι μόνο τα πλήκτρα.
- Insets για gesture navigation: ο χειρισμός γίνεται με `ViewCompat.setOnApplyWindowInsetsListener` και `WindowInsetsCompat`, προσθέτοντας το κάτω inset ως padding, χωρίς deprecated `systemUiVisibility` APIs.
- Enter: τιμά το `imeOptions` action (Go, Search, Send) εκτός αν το προφίλ ορίζει raw `KEYCODE_ENTER`, που είναι το default για τερματικά.
- Μέγεθος οθόνης: επαναϋπολογισμός γεωμετρίας σε `onSizeChanged` και `onConfigurationChanged`. Πλάτος κάτω από 600dp είναι phone mode, 600dp και πάνω επιτρέπει split mode. Το Jetpack WindowManager για fold posture μένει για το v1.1.
- `onDestroy`: ακυρώνει το `serviceScope`.
- Lifecycle API: `ServiceLifecycleDispatcher` από το `lifecycle-service` ώστε το service να εκθέτει `LifecycleOwner` όπου χρειάζεται.

### 4.6 Προφίλ ανά εφαρμογή

Χάρτης `packageName -> Profile(layoutId, numberRow, heightScale, enterSendsRawKey)`. Προεγκατεστημένα: `com.termux`, `org.connectbot`, `com.sonelli.juicessh`, `com.server.auditor.ssh.client` σε PC Full με raw Enter, και γνωστοί browsers σε compact. Τα built-in προφίλ είναι δωρεάν, γιατί είναι η βασική υπόσχεση του προϊόντος. Τα custom προφίλ είναι Pro στο v1.1.

Για να εμφανίσουμε ονόματα και εικονίδια εφαρμογών στις ρυθμίσεις χωρίς `QUERY_ALL_PACKAGES` (που το Play περιορίζει), δηλώνουμε στο manifest `<queries>` με intent `ACTION_MAIN` και `CATEGORY_LAUNCHER`. Αυτό δίνει πρόσβαση στις launchable εφαρμογές και επιτρέπεται από την πολιτική. Υπάρχει και χειροκίνητη εισαγωγή package name για ό,τι δεν είναι launchable.

### 4.7 Settings και onboarding σε Compose

Material 3 με dynamic color, MVVM με `StateFlow`. Οθόνες v1: Onboarding (ενεργοποίηση στο `Settings.ACTION_INPUT_METHOD_SETTINGS`, επιλογή με `InputMethodManager.showInputMethodPicker()`, δοκιμή σε πεδίο κειμένου), Αρχική, Layouts, Εμφάνιση, Pro. Η κατάσταση ενεργοποίησης διαβάζεται από `Settings.Secure.ENABLED_INPUT_METHODS` και `Settings.Secure.DEFAULT_INPUT_METHOD` με lifecycle-aware polling όσο η οθόνη onboarding είναι resumed. Navigation: Navigation 3 αν είναι stable τη στιγμή της υλοποίησης, αλλιώς sealed back stack σε Compose state, αφού μιλάμε για πέντε οθόνες. Το κείμενο του onboarding απαντά ρητά στην προειδοποίηση του συστήματος ότι ένα IME μπορεί να συλλέγει ό,τι πληκτρολογείς: καμία άδεια INTERNET, ανοιχτός κώδικας, σύνδεσμος στο repo.

### 4.8 Persistence

DataStore με custom `Serializer<T>` που χρησιμοποιεί kotlinx.serialization JSON. Έτσι έχουμε typed, migratable ρυθμίσεις χωρίς την protobuf toolchain. `ReplaceFileCorruptionHandler` επιστρέφει defaults σε `CorruptionException`. Τα layouts αποθηκεύονται ως JSON αρχεία σε `filesDir/layouts` μέσω `AtomicFile`. Όχι Room: δεν υπάρχουν σχέσεις, και η αρχικοποίηση βάσης στο ξεκίνημα του IME προσθέτει latency χωρίς αντάλλαγμα. Οι αλλαγές ρυθμίσεων φτάνουν στο IME μέσω του `Flow` του DataStore στο ίδιο process.

### 4.9 DI και Coroutines

Hilt. `@HiltAndroidApp` στο Application, `@AndroidEntryPoint` στο `InputMethodService` (υποστηρίζεται επειδή κληρονομεί από `Service`), `@Binds` για `BillingRepository` στα flavor source sets του `:app`. Γιατί Hilt και όχι Koin: compile-time επαλήθευση του γράφου, μηδέν reflection, και ο μόνος runtime κόστος είναι ένας μικρός γράφος. Γιατί όχι manual DI: το Hilt δεν κοστίζει τίποτα επιπλέον εδώ και κρατά το flavor binding δηλωτικό. Scope: `@Singleton` για repositories και DataStore, το service έχει το δικό του `CoroutineScope`.

Όλο το disk I/O και το parsing σε `Dispatchers.IO` και `Dispatchers.Default`. Το main thread του IME δεν μπλοκάρει ποτέ. `SupervisorJob` ώστε μία αποτυχία να μην ακυρώνει το scope, και `CoroutineExceptionHandler` που γράφει στο Logcat μόνο σε debug builds.

### 4.10 Billing και Pro gating

Play Billing Library, τελευταία major έκδοση (επιβεβαίωσε την απαίτηση του Play τη στιγμή του setup, το Play απαιτεί αναβάθμιση σε νέα major περιοδικά). Ένα one-time non-consumable product, `pro_unlock`.

Ροή: Pro οθόνη, σύνδεση `BillingClient`, `queryProductDetailsAsync`, `launchBillingFlow`, `PurchasesUpdatedListener`, έλεγχος `purchaseState == PURCHASED`, `acknowledgePurchase` (υποχρεωτικό εντός 3 ημερών, αλλιώς επιστροφή χρημάτων), αποθήκευση entitlement στο DataStore. Επαναφορά με `queryPurchasesAsync` κάθε φορά που ανοίγει η Pro οθόνη, και από το IME το πολύ μία φορά ανά 24 ώρες, εκτός main thread, με το αποτέλεσμα απλώς να ενημερώνει το cache.

Το IME δεν ανοίγει ποτέ σύνδεση `BillingClient` στο hot path. Διαβάζει το cached entitlement ως `StateFlow<ProEntitlement>`. Μια σύνδεση billing είναι binder round trip προς Play Services, και δεν έχει θέση στο ξεκίνημα ενός πληκτρολογίου. Αν το entitlement είναι άγνωστο, η εφαρμογή συμπεριφέρεται ως free και ποτέ δεν σπάει την πληκτρολόγηση.

Επαλήθευση υπογραφής αγοράς: η Google προτείνει server-side επαλήθευση. Χωρίς server, δεχόμαστε client-side έλεγχο του `purchaseState`. Το ρίσκο πειρατείας είναι αποδεκτό, ο κώδικας είναι ανοιχτός ούτως ή άλλως.

### 4.11 Κανόνες error handling και null safety

1. Το IME δεν κάνει ποτέ crash. Κάθε κλήση στον `KeyboardEngine` και στον `InputDispatcher` περνά από όριο που πιάνει exceptions, γυρίζει στο built-in default layout και εμφανίζει το σφάλμα στις ρυθμίσεις.
2. Το `currentInputConnection` είναι nullable και γίνεται null όταν δεν υπάρχει εστιασμένο πεδίο. Ο `InputDispatcher` κάνει no-op με ασφάλεια, ποτέ `!!`.
3. Αποτυχία parsing layout επιστρέφει `Result.failure` με δομημένο σφάλμα. Άκυρο custom layout σημαίνει fallback σε built-in και banner στις ρυθμίσεις, όχι κενό πληκτρολόγιο.
4. Billing: κάθε `BillingResponseCode` αντιστοιχίζεται σε `sealed interface BillingOutcome` (`Success`, `UserCancelled`, `Unavailable`, `AlreadyOwned`, `Error(code, message)`). Καμία αγορά δεν θεωρείται έγκυρη χωρίς `PURCHASED` και acknowledge.
5. DataStore: `ReplaceFileCorruptionHandler` με defaults, I/O αποκλειστικά σε `Dispatchers.IO`.
6. `EditorInfo` και `inputType` μπορούν να είναι απροσδόκητα (null packageName σε ορισμένα OEM). Κάθε ανάγνωση έχει default.

### 4.12 Performance

- Μηδενικές allocations σε `onDraw` και `onTouchEvent`. Προϋπολογισμένη γεωμετρία πλήκτρων σε `onSizeChanged`, cached `Paint` objects, cached πλάτη κειμένου.
- Μερικό invalidate μόνο της περιοχής του πατημένου πλήκτρου.
- Προεπισκόπηση μέσα στο View, όχι `PopupWindow`.
- Haptics με `performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)` και `KEYBOARD_RELEASE`, όχι απευθείας `Vibrator`: χαμηλότερο latency και σεβασμός στη ρύθμιση του συστήματος.
- Lazy parsing: μόνο το τρέχον layout στο ξεκίνημα, τα υπόλοιπα on demand σε `Dispatchers.Default`, μικρό in-memory cache.
- Τίποτα βαρύ στο `onCreate` του service: ο γράφος του Hilt είναι μικρός, το billing είναι εκτός διαδρομής.
- Baseline Profile στο v1.1, όταν θα υπάρχουν πραγματικές ροές για καταγραφή.
- Μέτρηση latency σε debug builds μέσω `Choreographer` frame callbacks από το touch μέχρι το frame, καταγραφή σε Logcat.

### 4.13 Accessibility

`ExploreByTouchHelper` (androidx.customview) εκθέτει κάθε πλήκτρο ως virtual view με content description, ώστε το TalkBack να δουλεύει με explore-by-touch. `announceForAccessibility` στην εναλλαγή layout και κατάστασης modifier. Ελάχιστο ύψος πλήκτρου 48dp στο default. High-contrast θέμα στο v1.1.

### 4.14 Privacy και permissions

- Καμία άδεια INTERNET. Καμία `QUERY_ALL_PACKAGES`. Το service δηλώνεται με `android:permission="android.permission.BIND_INPUT_METHOD"` και `android:exported="true"`, όπως απαιτεί το πλαίσιο IME.
- Σε password fields: καμία προεπισκόπηση, καμία αποθήκευση. Δεν υπάρχει ιστορικό πληκτρολόγησης πουθενά στην εφαρμογή.
- Data Safety: δεν συλλέγονται και δεν κοινοποιούνται δεδομένα. Το `play` flavor περιλαμβάνει μόνο Play Billing και In-App Review, που επεξεργάζονται δεδομένα υπό την πολιτική της Google και δηλώνονται ανάλογα.

### 4.15 Build, flavors, CI

- Kotlin 2.x με K2, AGP τελευταίο stable, version catalog, KSP για Hilt, Compose compiler plugin του Kotlin.
- Flavor dimension `distribution` με `play` και `foss`, ίδιο applicationId.
- R8 ενεργό και στα δύο flavors. Οι consumer rules της kotlinx.serialization καλύπτουν το serialization.
- Signing: Play App Signing με upload key για το `play`, δικό μας release key για το `foss` (GitHub Releases, IzzyOnDroid). Το F-Droid χτίζει και υπογράφει μόνο του από το `foss` flavor.
- GitHub Actions: σε κάθε PR build και στα δύο flavors, Android Lint, unit tests. Σε tag: AAB για Play internal track και APK για GitHub Release με checksums, upload στο Play με Gradle Play Publisher ή fastlane supply.
- Δομή `fastlane/metadata/android/` για περιγραφές και screenshots, που διαβάζουν και το F-Droid και το IzzyOnDroid.

### 4.16 Εξαρτήσεις Gradle

Pin τις τελευταίες stable εκδόσεις από Google Maven και Maven Central την ημέρα του setup. Δεν γράφω αριθμούς εκδόσεων εδώ για να μην παλιώσουν.

Plugins: `com.android.application`, `org.jetbrains.kotlin.android`, `org.jetbrains.kotlin.plugin.compose`, `org.jetbrains.kotlin.plugin.serialization`, `com.google.devtools.ksp`, `com.google.dagger.hilt.android`.

| Σκοπός | Artifact | Module |
|---|---|---|
| Core KTX | `androidx.core:core-ktx` | όλα |
| Lifecycle σε Service | `androidx.lifecycle:lifecycle-service` | :app |
| Lifecycle runtime, ViewModel Compose | `androidx.lifecycle:lifecycle-runtime-ktx`, `androidx.lifecycle:lifecycle-viewmodel-compose` | :settings |
| DataStore (custom serializer) | `androidx.datastore:datastore` | :ime |
| JSON | `org.jetbrains.kotlinx:kotlinx-serialization-json` | :ime |
| Coroutines | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | όλα |
| Compose | `androidx.compose:compose-bom`, `androidx.compose.material3:material3`, `androidx.compose.ui:ui`, `androidx.compose.ui:ui-tooling-preview`, `androidx.activity:activity-compose` | :settings |
| Navigation (αν stable) | `androidx.navigation3:navigation3-runtime`, `androidx.navigation3:navigation3-ui` | :settings |
| Hilt | `com.google.dagger:hilt-android`, `com.google.dagger:hilt-android-compiler` (ksp), `androidx.hilt:hilt-navigation-compose` | :app, :settings |
| Accessibility helper | `androidx.customview:customview` | :ime |
| Play Billing | `com.android.billingclient:billing-ktx` | :billing-play |
| In-App Review | `com.google.android.play:review-ktx` | :app (play) |
| Baseline Profiles (v1.1) | `androidx.profileinstaller:profileinstaller` | :app |

## 5. Χρονοδιάγραμμα: 14 εβδομάδες, περίπου 17 ώρες η καθεμία

| Εβδ. | Φάση | Παραδοτέα | Definition of done |
|---|---|---|---|
| 1 | Επαλήθευση | Καθημερινή χρήση Unexpected Keyboard και HeliBoard σε Termux, ανάγνωση #875, #939 και Reddit, λίστα top-10 κενών, επιλογή ονόματος, landing page με waitlist, post στο #875 και στο r/termux | Λίστα κενών γραμμένη, σελίδα live, posts δημοσιευμένα |
| 2 | Spike και go/no-go | Repo, modules, `InputMethodService` skeleton, Canvas view με στατικό layout που πληκτρολογεί σε Termux, μέτρηση latency | Ctrl+C και Esc δουλεύουν στο Termux, Gate 0 αποφασισμένο |
| 3 | Engine | Layout model και parser, γεωμετρία, touch σε πλήκτρο, `commitText` και `sendKeyEvent`, PC Full EN | Πληκτρολόγηση σε Termux και σε browser χωρίς λάθη χαρακτήρων |
| 4 | Engine | Modifiers state machine, chording, key repeat, long-press alternates, Backspace/Delete ως KeyEvents | Ctrl+Alt+Del, Ctrl+Shift+βελάκι και long-press σωστά σε Termux και σε Google Docs |
| 5 | Engine | Προεπισκόπηση μέσα στο view, `onComputeInsets`, insets gesture navigation, haptics, landscape | Μηδενικές allocations στο draw path, σωστή συμπεριφορά σε gesture και 3-button navigation |
| 6 | Layouts και θέμα | 4-row, compact, αριθμητικό, EL, Dvorak, Colemak, UK, Material You light/dark | Όλα τα layouts φορτώνουν από JSON, εναλλαγή EL/EN με πλήκτρο γλώσσας |
| 7 | Settings | Compose settings app, DataStore, ροή ρυθμίσεων στο IME, επιλογή layouts, ύψος, number row | Κάθε ρύθμιση εφαρμόζεται ζωντανά στο IME χωρίς restart |
| 8 | Onboarding και προφίλ | Onboarding 3 βημάτων, built-in προφίλ ανά εφαρμογή, password και number inputType handling, split mode 600dp+ | Νέος χρήστης ενεργοποιεί και επιλέγει το IME σε κάτω από 60 δευτερόλεπτα |
| 9 | Ποιότητα | ExploreByTouchHelper, crash-proofing με fallback layout, performance pass, Logcat latency metrics | TalkBack διαβάζει κάθε πλήκτρο, κανένα crash σε fuzz χειροκίνητης χρήσης 30 λεπτών |
| 10 | Flavors και billing | `play`/`foss`, `:billing-*`, Pro screen, entitlement cache, In-App Review hook, CI με GitHub Actions | Αγορά και επαναφορά δουλεύουν σε Play internal testing, το `foss` build είναι unlocked |
| 11 | Beta | Internal testing, μετά closed testing με 30 με 50 testers από το waitlist και το #875, device matrix (Samsung, Pixel, Xiaomi, ένα foldable) | Λίστα bugs ταξινομημένη, blockers μηδέν |
| 12 | Beta και store | Διορθώσεις, screenshots, listing, privacy policy, Data Safety, trader declaration, IzzyOnDroid submission, F-Droid merge request | Listing εγκεκριμένο στο Play, APK στο IzzyOnDroid |
| 13 | Launch | Production σε staged rollout 20%, 50%, 100% μέσα σε 5 ημέρες, ανακοινώσεις (βλ. 7.5) | v1.0 στο 100%, crash-free πάνω από 99.5% στα Play vitals |
| 14 | Buffer | Ό,τι γλίστρησε, πρώτες διορθώσεις από reviews | Backlog για v1.1 γραμμένο |

Συνολικά 210 με 280 ώρες. Το απρόβλεπτο κομμάτι είναι η εβδομάδα 11: τα OEM quirks (ύψος keyboard στο One UI, insets σε MIUI, συμπεριφορά σε foldable) δεν μπορούν να εκτιμηθούν πριν τα δεις. Αν η beta βγάλει περισσότερα από 10 blockers, μετακινείται το launch κατά μία εβδομάδα και καταναλώνεται το buffer. Δεν προστίθεται κανένα feature για να «αξιοποιηθεί» η καθυστέρηση.

## 6. Προϊόν και monetization

### 6.1 Free και Pro

Δωρεάν, για πάντα: όλα τα built-in layouts, modifiers και chording, F-keys, βελάκια, Material You και dark, haptics, landscape, split mode, built-in προφίλ για τερματικά, number row. Δηλαδή όλα όσα έκανε το Hacker's Keyboard. Αν κλειδώσεις την ίδια την πληκτρολόγηση, παίρνεις μονάστερες κριτικές και forks.

Pro, one-time: layout editor και απεριόριστα custom layouts, custom προφίλ ανά εφαρμογή, themes με import/export, backup/restore ρυθμίσεων, macros/snippets key, και στο v1.2 το prediction. Είναι τα features που ο ενθουσιώδης χρήστης θέλει αφού έχει ήδη αγαπήσει το προϊόν. Κίνητρο μετατροπής: ο free χρήστης βλέπει τον editor και μπορεί να αποθηκεύσει ένα custom layout, το δεύτερο ζητά Pro.

### 6.2 Τιμολόγηση

Τιμή εκκίνησης 4,99 €, με τιμή launch 3,49 € τις δύο πρώτες εβδομάδες. Τα utilities one-time ζουν στη ζώνη 3 με 6 €. Κάτω από 3 € δείχνει φθηνό και ρίχνει την αντιληπτή αξία, πάνω από 6 € χάνει τους impulse buyers. Το Play εφαρμόζει τοπική τιμολόγηση αυτόματα. Παράδειγμα Ελλάδας: 4,99 € με ΦΠΑ 24% είναι 4,02 € καθαρά, μείον 15% προμήθεια Google (κάτω από 1 εκατ. $ τον χρόνο) μένουν περίπου 3,42 € ανά πώληση. Η Google είναι merchant of record και αποδίδει τον ΦΠΑ στην ΕΕ.

### 6.3 Open-core: γιατί όλα ανοιχτά

Ολόκληρος ο κώδικας Apache-2.0, μαζί με τα Pro features. Το `foss` flavor είναι ξεκλείδωτο. Πληρώνεις στο Play για την ευκολία της εγκατάστασης και των ενημερώσεων και για να στηρίξεις το project. Τρεις λόγοι: ένα πληκτρολόγιο βλέπει ό,τι πληκτρολογείς και το κοινό μας δεν εμπιστεύεται closed source. Αν κρατούσαμε κλειστά τα Pro features, η κοινότητα θα τα έγραφε σε fork σε δύο εβδομάδες. Και η επαλήθευση developer που φέρνει η Google στο Android το 2026 με 2027 κάνει το sideloading και το F-Droid πιο δύσκολα, άρα το Play γίνεται η βολική διαδρομή και η μετατροπή ανεβαίνει χωρίς να κλειδώσουμε τίποτα. Στο `foss` build υπάρχει σύνδεσμος δωρεάς. Στο `play` build δεν υπάρχει εξωτερικός σύνδεσμος πληρωμής, η πολιτική του Play το απαγορεύει.

### 6.4 Μετρήσεις χωρίς analytics

Χωρίς INTERNET δεν υπάρχει analytics SDK. Μετράμε από το Play Console: installs και uninstalls, ratings και reviews, crash-free και ANR rate από τα vitals, μετατροπές IAP. Τοπικοί μετρητές (ημέρες χρήσης, πατήματα) ζουν μόνο στη συσκευή και χρησιμεύουν για τον χρόνο εμφάνισης του In-App Review prompt. Ό,τι άλλο χρειαζόμαστε το μαθαίνουμε από GitHub issues και από την κοινότητα.

### 6.5 Οικονομικές προσδοκίες

Εκτιμήσεις, όχι προβλέψεις, με καθαρό έσοδο περίπου 3,4 € ανά πώληση:

| Σενάριο | Pro πωλήσεις πρώτου έτους | Καθαρό έσοδο |
|---|---|---|
| Απαισιόδοξο | 500 | περίπου 1.700 € |
| Βασικό | 2.000 | περίπου 6.800 € |
| Αισιόδοξο | 6.000 | περίπου 20.000 € |

Το προϊόν δεν είναι επιχείρηση πλήρους απασχόλησης. Είναι ένα σταθερό πρώτο προϊόν με χαμηλό ρίσκο που χτίζει ιστορικό στο Play, κριτικές και κεφάλαιο εμπιστοσύνης στην κοινότητα, και χρηματοδοτεί το επόμενο, μεγαλύτερο στοίχημα.

## 7. Go-to-market

### 7.1 Επαλήθευση, εβδομάδες 1 με 2

- Επτά ημέρες καθημερινής χρήσης Unexpected Keyboard και HeliBoard σε Termux. Αν το Unexpected Keyboard έχει κλείσει το κενό το 2025 με 2026, το μαθαίνουμε εδώ και όχι στην εβδομάδα 12.
- Ανάγνωση και κωδικοποίηση όλων των σχολίων στα #875 (42 σχόλια) και #939 και των σχετικών νημάτων σε r/termux και r/androidapps. Παραδοτέο: top-10 ζητούμενα features με συχνότητα.
- Landing page σε δικό μας domain (GitHub Pages ή Vercel), με το value proposition σε μία πρόταση, τρία screenshots από mockup, waitlist μέσω απλής φόρμας. Στόχος 100 με 200 emails σε δύο εβδομάδες.
- Post στο #875 και στο r/termux ως ερώτηση και όχι ως διαφήμιση: «χτίζω διάδοχο από το μηδέν, αυτό είναι το πλάνο, τι σας λείπει;». Έλεγχος κανόνων self-promotion πριν από κάθε post.
- Κριτήριο διακοπής: κάτω από 100 εγγραφές και χωρίς σαφές ποιοτικό σήμα από τα posts, το project σταματά στο Gate 0.

### 7.2 Όνομα και brand

Κριτήρια: δεν περιέχει «Hacker» ούτε «pckeyboard», μία ή δύο συλλαβές, προφέρεται και γράφεται εύκολα διεθνώς, διαθέσιμο domain .app ή .dev, καμία σύγκρουση στο Play Store, καμία εγγραφή σε κλάση 9 στο TMview (EUIPO) και στο USPTO. Ένα όνομα με ελληνική χροιά είναι πλεονέκτημα διαφοροποίησης αν γράφεται εύκολα με λατινικούς χαρακτήρες. Το εικονίδιο δείχνει ένα πλήκτρο Ctrl ή Esc, όχι γενικό πληκτρολόγιο.

### 7.3 Κανάλια, κατά σειρά απόδοσης

1. Το issue tracker του Hacker's Keyboard: #875, #939, #997. Οι χρήστες εκεί ήδη ψάχνουν.
2. r/termux, Termux Discord και Matrix, η σελίδα για πληκτρολόγια στο Termux wiki (PR με προσθήκη).
3. r/androidapps, r/fossdroid, r/degoogle.
4. Hacker News, Show HN Τρίτη με Πέμπτη πρωί ώρα ΗΠΑ, με τεχνικό blog post «γιατί ξαναέγραψα το Hacker's Keyboard από το μηδέν» (latency, KeyEvents, modifiers). Το HN ανταμείβει το τεχνικό βάθος.
5. IzzyOnDroid (λίστα σε ημέρες) και F-Droid (merge request με εβδομάδες αναμονής, ξεκινά στην εβδομάδα 12).
6. Fediverse/Mastodon, όπου ζει το FOSS κοινό.
7. Καταχώριση στο AlternativeTo ως εναλλακτική του Hacker's Keyboard.

### 7.4 Store listing

Τίτλος έως 30 χαρακτήρες με το όνομα και τη λέξη-κλειδί, π.χ. «<Όνομα>: PC Keyboard». Σύντομη περιγραφή: «Full PC layout keyboard for Termux, SSH and coding. Ctrl, Esc, Tab, arrows, F-keys. Open source, no internet permission.» Screenshots: Termux με vim, SSH σε server, Google Sheets με βελάκια, tablet split mode, dark mode. Λέξεις-κλειδιά στην περιγραφή: terminal keyboard, Termux keyboard, SSH keyboard, Ctrl Esc Tab, arrow keys, F keys, Hacker's Keyboard alternative, programmer keyboard.

### 7.5 Ακολουθία launch

Εβδομάδα 10: internal testing. Εβδομάδα 11 με 12: closed testing με 30 με 50 άτομα από waitlist και #875, δύο εβδομάδες. Εβδομάδα 13: production σε staged rollout 20%, 50%, 100% μέσα σε πέντε ημέρες, και την ίδια μέρα του 100%: σχόλιο στο #875, posts σε r/termux, r/androidapps, r/fossdroid, Show HN, Fediverse, blog post, email στο waitlist.

### 7.6 Μετά το launch

In-App Review prompt μετά από 7 ημέρες χρήσης και 2.000 πατήματα (τοπικοί μετρητές). Απάντηση σε κάθε review τον πρώτο μήνα. Μηνιαία release cadence. Σελίδα «Hacker's Keyboard alternative 2026» στο domain μας για SEO. Οδηγός «το κινητό ως τερματικό» με setup Termux και το πληκτρολόγιο. Μηνιαία ανασκόπηση των GitHub issues για να τροφοδοτείται το v1.1.

## 8. Διαχειριστικά και νομικά

### 8.1 Όνομα και trademark

Έλεγχος στο TMview, στο USPTO, στο Play Store και σε domains πριν το πρώτο δημόσιο post. Κατοχύρωση EUTM σε μία κλάση (κόστος της τάξης των 850 €) μόνο αν το ετήσιο έσοδο ξεπεράσει περίπου 5.000 €. Μέχρι τότε αρκεί η χρήση και η παρουσία. Δεν είναι νομική συμβουλή.

### 8.2 Άδεια και υγιεινή repo

Apache-2.0 με αρχείο NOTICE. DCO (`Signed-off-by`) αντί για CLA: είμαστε solo, όλα είναι ανοιχτά, δεν χρειαζόμαστε δικαίωμα relicensing. CONTRIBUTING.md, SECURITY.md, issue templates για bug και layout request, CODE_OF_CONDUCT. Το repo γίνεται δημόσιο από το πρώτο commit: χτίζει εμπιστοσύνη και φέρνει testers νωρίς.

### 8.3 Play Console

- Υπάρχον developer account. Έλεγχος τύπου λογαριασμού: ο κανόνας των 12 testers για 14 ημέρες ισχύει μόνο για personal accounts που δημιουργήθηκαν μετά τις 13/11/2023.
- Trader status (EU DSA): με πωλήσεις IAP δηλώνεσαι trader και το Play εμφανίζει δημόσια όνομα, διεύθυνση, τηλέφωνο και email. Χρειάζεται επαγγελματική διεύθυνση ή virtual office, αφιερωμένο email και τηλέφωνο, πριν το listing.
- D-U-N-S απαιτείται μόνο για organization accounts.
- App content: χωρίς διαφημίσεις, όχι για παιδιά, Data Safety «no data collected», privacy policy URL υποχρεωτικό.
- Υπενθύμιση κάθε Αύγουστο: νέα απαίτηση targetSdk στο Play. Προϋπολογισμός 2 εβδομάδων τον χρόνο για συντήρηση έναντι πλατφόρμας.

### 8.4 Privacy policy και Data Safety

Σελίδα στο domain μας: δεν συλλέγονται δεδομένα, η εφαρμογή δεν έχει άδεια INTERNET, το `play` build περιέχει Play Billing και In-App Review της Google, το `foss` build τίποτα. Ενημέρωση σε κάθε αλλαγή εξαρτήσεων.

### 8.5 Φορολογικά, όχι συμβουλή

Οι πληρωμές από το Play έρχονται ως payouts από την Google, που είναι merchant of record. Η συνήθης πρακτική στην Ελλάδα για ατομική επιχείρηση είναι να αντιμετωπίζονται ως ενδοκοινοτική παροχή υπηρεσιών, που απαιτεί εγγραφή στο VIES, ανακεφαλαιωτικούς πίνακες και καταχώριση στο myDATA. Επιβεβαίωση με τον λογιστή πριν την πρώτη πληρωμή, και ρύθμιση των payout στοιχείων στο Play Console στην επωνυμία της επιχείρησης.

### 8.6 Signing και backups

Play App Signing με upload key. Upload key, foss release key και passwords σε password manager και σε offline αντίγραφο. Τα secrets του CI σε GitHub Actions secrets, ποτέ στο repo.

## 9. Ρίσκα και μετριασμός

| Ρίσκο | Πιθανότητα | Μετριασμός |
|---|---|---|
| Το Unexpected Keyboard έχει ήδη κλείσει το κενό | Μέτρια | Επαλήθευση εβδομάδας 1, διαφοροποίηση σε πιστότητα PC layout, chording, προφίλ ανά εφαρμογή, UX onboarding |
| Χαμηλή μετατροπή σε Pro από FOSS κοινό | Υψηλή | Pro features ως «απολαύσεις ενθουσιώδους», ευκολία Play, δοκιμές τιμής, σαφές μήνυμα «πληρώνεις για να συνεχιστεί» |
| OEM quirks (One UI, MIUI, foldables) | Υψηλή | Beta device matrix, Samsung πρώτο επειδή έχει το μεγαλύτερο μερίδιο, insets μέσω WindowInsetsCompat |
| Τράπεζες μπλοκάρουν third-party IMEs | Σίγουρη για κάποιες | Δεν διορθώνεται από εμάς. Τεκμηρίωση στο FAQ, καμία υπόσχεση |
| Πειρασμός για Compose στο IME window | Μέτρια | Απόφαση κλειδωμένη στο 4.2, το `:ime` δεν έχει Compose dependency |
| Scope creep (prediction, clipboard, emoji) | Υψηλή | Λίστα Won't, decision gate πριν από κάθε μεγάλο feature, κανένα feature χωρίς ενημέρωση αυτού του αρχείου |
| Ετήσιος «φόρος» targetSdk και πολιτικών | Σίγουρη | Ημερολόγιο Αυγούστου, 2 εβδομάδες τον χρόνο προϋπολογισμένες |
| Crash που αφήνει τον χρήστη χωρίς πληκτρολόγιο | Χαμηλή με τους κανόνες του 4.11 | Fallback layout, όρια exceptions, staged rollout, παρακολούθηση vitals |
| Χρόνος: 15 με 20 ώρες δίπλα σε πλήρη εργασία | Μέτρια | Εβδομαδιαίο cap, buffer εβδομάδα, ρητός ορισμός maintenance mode |
| Αρνητικές κριτικές για TalkBack | Χαμηλή | ExploreByTouchHelper στο v1 |
| Πειρατεία Pro | Σίγουρη | Αποδεκτή, ο κώδικας είναι ανοιχτός ούτως ή άλλως |

## 10. Decision gates

- Gate 0, τέλος εβδομάδας 2: τουλάχιστον 100 εγγραφές στο waitlist ή σαφές ποιοτικό σήμα από #875 και r/termux, και spike που αποδεικνύει Ctrl+C και Esc σε Termux. Αλλιώς stop, με κόστος δύο εβδομάδων.
- Gate 1, 8 εβδομάδες μετά το launch: βαθμολογία 4,3 και πάνω, crash-free 99,5% και πάνω, τουλάχιστον 300 Pro πωλήσεις. Αν περάσει, ξεκινά το v1.2 με prediction. Αν όχι, v1.1 ολοκληρώνεται και το project μπαίνει σε maintenance mode.
- Gate 2, μήνας 9: καθαρό έσοδο προς ώρες. Αν είναι κάτω από 15 € την ώρα, maintenance mode μόνιμα και ο χρόνος πάει στο επόμενο προϊόν. Το πληκτρολόγιο συνεχίζει να πουλά και να συντηρείται.

Maintenance mode σημαίνει: ενημέρωση targetSdk κάθε χρόνο, διορθώσεις crash, merge PRs από την κοινότητα, τίποτα άλλο.

## 11. Backlog (parking lot)

- On-device prediction και autocorrect με λεξικά ανά γλώσσα (Pro, v1.2, μόνο μετά το Gate 1).
- Macros/snippets key: `cd ~/projects`, `git status`, custom εντολές (Pro, v1.1).
- Mic key που εναλλάσσει στο voice IME του συστήματος.
- Clipboard history αποκλειστικά στη μνήμη, opt-in.
- Emoji μέσω alternates ή system picker.
- Jetpack WindowManager για fold posture.
- Themes marketplace: όχι, ποτέ.
- Gesture typing: όχι, ποτέ.
- Physical keyboard remapping: διαφορετικό προϊόν, όχι.
- iOS: όχι.

## 12. Ανοιχτές ερωτήσεις

1. Όνομα και domain.
2. Ποιες γλώσσες πέρα από EN και EL στο v1; Κάθε γλώσσα είναι data, αλλά χρειάζεται native tester.
3. Αγορά μεταχειρισμένων συσκευών για το device matrix (ένα Samsung μεσαίας κατηγορίας, ένα Xiaomi, ένα foldable) ή αποκλειστική εξάρτηση από beta testers;
4. Hosting για landing page και privacy policy: GitHub Pages ή Vercel;
5. Επαγγελματική διεύθυνση για το trader declaration.

## 13. Συμβάσεις υλοποίησης (για το Claude Code)

- Αυτό το αρχείο είναι η πηγή αλήθειας για scope και αρχιτεκτονική. Αναφορά σε αυτό από το CLAUDE.md. Κάθε αλλαγή scope ή νέο module ενημερώνει πρώτα το PLAN.md.
- Kotlin official code style. Explicit API mode στα library modules. Κανένα `!!`. Nullable τιμές λύνονται στα όρια (InputConnection, EditorInfo, parsing), όχι στο εσωτερικό του engine.
- Κανένα deprecated API. Έλεγχος `@Deprecated` σε κάθε κλήση πλαισίου, προτίμηση σε `WindowInsetsCompat`, `ViewCompat`, `ServiceLifecycleDispatcher`.
- Το `:ime` δεν εισάγει Compose, billing ή Play Services. Το `KeyboardEngine` δεν έχει Android imports εκτός από σταθερές `KeyEvent`.
- Structured concurrency μόνο: κάθε coroutine σε scope με ιδιοκτήτη, καμία `GlobalScope`, κανένα `runBlocking` σε production κώδικα.
- Σφάλματα ως sealed types ή `Result`, ποτέ exceptions ως control flow πέρα από τα όρια.
- Καμία allocation σε `onDraw` και `onTouchEvent`. Κάθε αλλαγή στο `KeyboardView` συνοδεύεται από χειροκίνητο έλεγχο latency σε συσκευή και σημείωση στο PR.
- Κανένα string hardcoded στο UI, όλα σε resources, EL και EN από το v1.
- Κώδικας χωρίς inline σχόλια. Η τεκμηρίωση ζει σε αυτό το αρχείο και σε KDoc μόνο για δημόσια API των modules.
- Commits με DCO sign-off, conventional commits.
