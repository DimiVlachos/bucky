# Πλάνο: πληκτρολόγιο τύπου PC για Android

Όνομα: **Bucky** (από τα *bucky bits*, τα bits που θέτουν τα Ctrl/Alt/Meta/Super), εμπορικά «Bucky Keys», domain `buckykeys.app`. Εκκρεμεί έλεγχος TMview/USPTO κλάση 9 (βλ. 8.1). Έκδοση πλάνου 1.0, 18 Οκτωβρίου 2026.
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
| Διατάξεις 4 και 5 σειρών, πλήρης PC (Esc, Tab, Ctrl, Alt, Fn, βελάκια, Home/End/PgUp/PgDn, Del, Ins), και κλασική Hacker's Keyboard με ❖ Meta και σελίδα Fn (2026-10-27, βλ. 4.3) | Compact διάταξη για browsers και chat (έγινε, 2026-10-25, βλ. 4.6) | Prediction, autocorrect, swipe |
| Modifiers: one-shot, locked, chording με multi-touch, και Fn layer με F1 έως F12, PgUp/PgDn, Home/End και Ins (αίτημα του ketas στο #875, 2026-10-25) | Media keys στο Fn layer | Clipboard history |
| Σωστή αποστολή KeyEvents με meta state (Ctrl+C, Esc, βελάκια) ώστε να δουλεύουν Termux, vim, SSH | Αυτόματη εναλλαγή σε compact σε password fields | Emoji keyboard |
| Layouts EN (US, UK), EL, Dvorak, Colemak, όλα data-driven | DE και FR layouts αν υπάρχει native tester | Voice input |
| Long-press alternates, key repeat, haptics | Αριθμητικό layout για inputType number/phone | Themes πέρα από Material You και dark |
| Built-in προφίλ για Termux, ConnectBot, JuiceSSH, Termius (έγινε, 2026-10-25, βλ. 4.6) | Ρύθμιση ύψους και split mode σε πλάτος 600dp+ | Layout editor (v1.1, Pro) |
| Material You dynamic colors, light και dark | Προεπισκόπηση πλήκτρου μέσα στο view, όχι PopupWindow | Custom per-app προφίλ (v1.1, Pro) |
| Onboarding 3 βημάτων: enable, select, try | Backup ρυθμίσεων σε αρχείο (v1.1, Pro) | Physical keyboard remapping |
| Προαιρετική μόνιμη σειρά F1 έως F12, εκτός από το Fn layer (αίτημα από #875 και r/termux) | | |
| Προαιρετική επιπλέον σειρά με όποια πλήκτρα ή σύμβολα διαλέξει ο χρήστης, δωρεάν (ο layout editor μένει Pro) | | |
| Landscape με δικό του ύψος. Το πλάτος μετριέται από το window και τα insets σε κάθε layout pass, με χειροκίνητη ρύθμιση πλάτους και δεξιού padding ως fallback για OEM builds (hackerskeyboard#957) | | |
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
:settings        KeyboardSettings και το DataStore repository τους, Compose: onboarding, settings, προφίλ ανά εφαρμογή, Pro screen, layout editor (v1.1)
:billing         Interfaces: ProEntitlement, BillingRepository, sealed BillingOutcome
:billing-play    Υλοποίηση με Play Billing Library
:billing-foss    Υλοποίηση που επιστρέφει πάντα Unlocked
```

Γιατί έξι και όχι περισσότερα: το `:ime` δεν εξαρτάται από Compose ούτε από billing, κρατά τον build χρόνο χαμηλό και εγγυάται ότι τίποτα βαρύ δεν μπαίνει στο IME window. Το billing χωρίζεται σε modules επειδή το `foss` flavor δεν πρέπει να περιέχει τη βιβλιοθήκη της Google (απαίτηση F-Droid), και ο καθαρότερος τρόπος είναι `playImplementation(project(":billing-play"))` και `fossImplementation(project(":billing-foss"))` στο `:app`. Convention plugins και build-logic: όχι στο v1. Με έξι modules η επανάληψη στα build scripts είναι ανεκτή, και η επένδυση σε build υποδομή πριν βγει το προϊόν είναι ακριβώς το μοτίβο που σκότωσε το Ivy Wallet. Κανένα νέο module χωρίς ενημέρωση αυτού του αρχείου.

Εξαίρεση εργαλείων build: το `:detekt-rules` (custom detekt rules από άλλα projects του Dimitris: NoNotNullAssertion, CatchBlockHygiene, TimberErrorArgument, HardcodedColor) είναι Kotlin/JVM module μόνο για static analysis, δεν μπαίνει στο APK και δεν μετράει στα έξι modules του προϊόντος.

### 4.2 Rendering: Canvas, όχι Compose, στο IME window

Το πληκτρολόγιο είναι ένα custom `View` που ζωγραφίζει σε Canvas. Το Compose μέσα σε `InputMethodService` απαιτεί χειροκίνητο wiring των `LifecycleOwner`, `ViewModelStoreOwner` και `SavedStateRegistryOwner` στο decor view του IME window και φέρνει κόστος recomposition σε κάθε key press. Σε πληκτρολόγιο ο προϋπολογισμός από το άγγιγμα μέχρι το glyph είναι ένα frame, και με Canvas το κόστος ανά frame είναι ντετερμινιστικό. Το Compose χρησιμοποιείται κανονικά στο Activity των ρυθμίσεων.

Η προεπισκόπηση πλήκτρου (η μεγεθυμένη ετικέτα πάνω από το πλήκτρο, για κάθε πλήκτρο εκτός από τους modifiers, που φωτίζονται ήδη όσο κρατιούνται, και για το τελευταίο δάχτυλο· το space δείχνει ␣ και οι ετικέτες-λέξεις όπως Enter ή PgUp κρατούν το πλήρες μέγεθος και φαρδαίνουν την προεπισκόπηση ως τρεις σειρές, μικραίνοντας μόνο πέρα από αυτό, με πλάτη και μεγέθη που υπολογίζονται μαζί με τις ετικέτες· αιτήματα του ketas στο #57, 2026-10-28 και 2026-10-30· κρύβεται όταν το δάχτυλο φύγει από το πλήκτρο ή ανοίξει το popup του long-press) ζωγραφίζεται μέσα στο ίδιο View και όχι με `PopupWindow`: τα popups είναι ξεχωριστά windows με δικό τους latency και jank. Το View επεκτείνεται πάνω από τα πλήκτρα και με `onComputeInsets` δηλώνουμε ως touchable region μόνο την περιοχή των πλήκτρων.

Τα special keys (Esc, Tab, Enter, ⌫, Del, βελάκια, Home/End), οι modifiers, τα πλήκτρα αλλαγής σελίδας και το space ζωγραφίζονται σε πιο σκούρο φόντο από τους χαρακτήρες, όπως στο Hacker's Keyboard (αίτημα του ketas, 2026-10-27· το space και μεγαλύτερη αντίθεση στο σκούρο θέμα, 2026-10-28). Το space δείχνει τη γλώσσα και ένα ␣ από κάτω, όπως στο Hacker's Keyboard, κεντραρισμένα κάθετα με βάση το μελάνι των γλυφών και όχι τα font metrics (ketas, 2026-10-29). Το popup του long-press είναι ένα πλαίσιο με δικό του φόντο, περίγραμμα και μια λεπτή γραμμή accent στο κάτω μέρος, χωρίς φόντο πλήκτρου ανά επιλογή, και η επιλογή έχει περίγραμμα σε accent, με κενό τεσσάρων gaps πάνω από τη γραμμή (ketas, 2026-10-29), ώστε να μη μοιάζει με τη σειρά πλήκτρων που καλύπτει (ketas, 2026-10-28). Το ποια πλήκτρα είναι special υπολογίζεται μία φορά όταν χτίζονται οι ετικέτες, ώστε το `onDraw` να διαλέγει μόνο paint.

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

Κανόνες του format (`LayoutParser`, εβδομάδα 3):

- Κάθε πλήκτρο έχει ακριβώς ένα από τα `chars`, `key` ή `switchTo`. Το `key` παίρνει ονόματα του `KeyCode` (`ESC`, `F1`, `HOME`) και του `ModifierKind` (`CTRL`, `ALT`, `SHIFT`). Το `FN` είναι modifier όπως τα άλλα (one-shot, locked, κράτημα) και επιτρέπεται και στη σειρά του χρήστη. Το προαιρετικό `fn` ενός πλήκτρου ορίζει τι στέλνει όταν είναι ενεργό το Fn, με τα ίδια ονόματα όπως το `longPress` (π.χ. `"fn": "PAGE_UP"`).
- `chars` με ένα string παίρνει ως shifted το κεφαλαίο του. Το `longPress` επιτρέπεται μόνο μαζί με `chars`· κάθε στοιχείο του που είναι όνομα `KeyCode` (`F1`, `ESC`) γίνεται το αντίστοιχο πλήκτρο, οτιδήποτε άλλο γίνεται χαρακτήρας με κεφαλαίο το uppercase του. Το PC Full έχει F1 έως F12 ως long-press στη σειρά αριθμών.
- Το `label` είναι προαιρετικό: default είναι ο πρώτος χαρακτήρας, κενό για το space, και σταθερές ετικέτες για τα special keys (Esc, ⌫, Ctrl, και σύμβολα ⇧ και ⇥ για Shift και Tab, όπως ζήτησε ο ketas, 2026-10-27). Το `width` είναι προαιρετικό, default 1, πάντα θετικό.
- `dead` ορίζει dead key με ένα ή δύο σημάδια (π.χ. `["΄", "¨"]` για τόνο και διαλυτικά). Ο parser υπολογίζει κατά τη φόρτωση, με Unicode NFKD/NFC, σε τι συντίθεται κάθε γράμμα του layout με κάθε σημάδι, άρα δεν υπάρχει πίνακας ανά γλώσσα να συντηρείται και το engine κάνει μόνο lookup. Σημάδι και μετά γράμμα χωρίς σύνθεση δίνει και τα δύο, το ίδιο σημάδι δύο φορές ή σημάδι και space δίνει το σημάδι, Backspace το ακυρώνει. Ένα dead key δέχεται και `longPress`, ώστε το απλό σημάδι να βγαίνει με ένα κράτημα (αίτημα του ketas στο #57, 2026-10-28): στην ET το ˇ/~ δίνει ~ ` ˇ και το ´/` δίνει ` F12 ´, και στη FI το ¨/^ δίνει ^ ~ ¨.
- `latin` δίνει σε μη λατινικό πλήκτρο το λατινικό γράμμα της θέσης του, για Ctrl και Alt: Ctrl+ψ στέλνει Ctrl+C όπως σε PC με ελληνική διάταξη.
- Η γλώσσα αλλάζει με swipe στο space: δεξιά η επόμενη, αριστερά η προηγούμενη, όπως στο Hacker's Keyboard και στο Gboard, και το space δείχνει το όνομα της γλώσσας (πεδίο `language` του layout, default το `name`) στη σελίδα των γραμμάτων και στη σελίδα Fn (που μένει ανοιχτή μετά από ένα swipe), και με μία μόνο γλώσσα, ώστε να φαίνεται ποια διάταξη είναι ενεργή (αίτημα του ketas στο #57, 2026-10-27). Τα built-in layouts δεν έχουν πλήκτρο 🌐, και οι θέσεις ακολουθούν το Hacker's Keyboard (#51 του netllama και αίτημα του ketas στο #875, απόφαση 2026-10-26): το `` ` ``/~ ξεκινά τη σειρά αριθμών (⌫ 2), το Esc είναι κάτω αριστερά, και τα πλήκτρα δεξιά στενεύουν (Tab 2 και `\` 1, Ctrl 2,25 και Enter 1,75, Shift 2,5), ώστε τα γράμματα να μοιράζονται στα δύο χέρια: t|y στο 7, g|h στο 7,25, b|n στο 7,5 από τα 15 πλήκτρα, με το 7,5 στο κέντρο. Η κάτω σειρά είναι Esc 1,25, Alt 1,25, Home 1,5, End 1,5, space 4,5, Fn 1,25, ← ↓ → 1,25 (το Fn δεξιά του space, όπως στο Hacker's Keyboard), και το space απέχει 0,25 πλήκτρα από το κέντρο. Ένα test κρατά κάθε σειρά γραμμάτων μέσα σε μισό πλήκτρο από το κέντρο και το space μέσα σε ένα τέταρτο. Τα βελάκια μένουν 1,25 ώστε το ↓ να είναι κάτω από το ↑ και το → κάτω από το Del. Το `switchTo: "@language"` (πλήκτρο 🌐) μένει διαθέσιμο σε custom layouts και περνά στην επόμενη γλώσσα. Άλλοι στόχοι `switchTo`: `@page:letters`, `@page:symbols`, `@page:symbols-more` (σελίδες της συμπαγούς) και `@keyboard` (⌨, με `"holdTo": "@full"`). Μια σειρά δέχεται `"inset"` (κενό στις άκρες, σε μονάδες πλήκτρου· τα αγγίγματα εκεί πάνε στο ακριανό πλήκτρο) και ένα πλήκτρο `"then"` (αλλαγή σελίδας αμέσως μετά). Hint δείχνεται όταν το πρώτο alternate δεν είναι το κεφαλαίο του πλήκτρου. Το PC Full EN και το PC Full EL έχουν τα ίδια πλήκτρα στις ίδιες θέσεις, και ένα test το επιβάλλει. Στο ελληνικό layout το € είναι long-press στο ε (απόφαση 2026-10-24). Το PC Full ET ακολουθεί την τυπική εσθονική διάταξη στις ίδιες θέσεις: ü õ μετά το p, ö ä μετά το l, το ' * στη θέση του `\`, dead keys ˇ/~ πάνω αριστερά και ´/` στη θέση του =, και το πλήκτρο < > αριστερά του z (Shift 1,5, ώστε το b|n να μένει στο 7,5). Αφού δεν υπάρχει AltGr, τα σύμβολά του (@ £ $ € { [ ] } \ | § ½, και š ž) είναι το πρώτο long-press του πλήκτρου που τα έχει στο φυσικό πληκτρολόγιο, ώστε να φαίνονται ως hint (απόφαση 2026-10-26). Ένα dead key δέχεται `fn` (το ´ δίνει F12), και το ˇ συνθέτει με τον combining caron. Η συμπαγής ET έχει q έως p ü õ και a έως l ö ä, όπως το Gboard.
- Φινλανδικά (FI, αίτημα του ketas, που γράφει στο φινλανδικό του Hacker's Keyboard, απόφαση 2026-10-27): η κεντραρισμένη `pc_full_fi` είναι η τυπική φινλανδική διάταξη στο πλέγμα της ET (`§½` πάνω αριστερά, `+?` και dead ´/`, å και dead ¨/^, ö ä, `'*`, `<>` αριστερά του z), με τα σύμβολα AltGr στο πρώτο long-press όπως στην ET. Επειδή ένα dead key κρατά δύο σημάδια, το `~` (AltGr+¨ στο φυσικό πληκτρολόγιο) είναι long-press στο `'*`. Η κλασική `pc_hk_fi` αντιγράφει τη φινλανδική του HK πλήκτρο προς πλήκτρο: χωρίς dead keys, `?+` και `'`` ` στη σειρά αριθμών, `~^` δίπλα στο å. Η συμπαγής `compact_fi` έχει q έως p å και a έως l ö ä.
- Κλασική διάταξη Hacker's Keyboard (αίτημα του ketas, απόφαση του Dimitris 2026-10-27, σχεδιασμός στο `docs/specs/2026-10-27-hk-classic-layout-design.md`): δεύτερη πλήρης διάταξη, `pc_hk_en_us`, `pc_hk_el`, `pc_hk_et`, με τις ακριβείς θέσεις του `kbd_full.xml` του HK σε πλέγμα 14 πλήκτρων (στενό `` ` ``, Tab 1, Ctrl και Enter 1,5, Shift 2 και δεξί Shift, κάτω σειρά Esc 1,5, ⚙, Alt, ❖ Meta, space 4,5, Fn, ○, βελάκια, χωρίς Home/End/Del στο κύριο layer). Το ET έχει το ISO `<>` αριστερά του z με Shift 1, όπως το φινλανδικό του HK. Το ⚙ (`switchTo: "@settings"`) είναι στη θέση του πλήκτρου επιλογών του HK και ανοίγει τις ρυθμίσεις του Bucky, ώστε το Esc να έχει το πλάτος 1,5 του HK (ο ketas βρήκε το Esc 2,5 υπερβολικά μεγάλο, απόφαση 2026-10-27). Το Fn ανοίγει τη σελίδα `pc_hk_fn` (`@page:fn`), όπως το `kbd_full_fn.xml`: F1 έως F12 σε μπλοκ 4×3, numpad, Home/End/PgUp/PgDn, Ins/Del, SysRq/ScrLk/Brk/NumLk, με τη συμπεριφορά modifier του Fn (one-shot, lock, hold). Ένας κρατημένος modifier μένει κρατημένος όταν αλλάζει η σελίδα κάτω από το δάχτυλο. Το ❖ Meta είναι modifier όπως Ctrl και Alt (`META_META_ON`), και το ○ στέλνει `DPAD_CENTER`. Η κεντραρισμένη διάταξη μένει η προεπιλογή και κρατά το Fn ανά πλήκτρο.
- Επιπλέον σειρές (`Layout.withExtraRows`, ρυθμίσεις «Σειρά F1–F12» και «Δική σας σειρά»): η σειρά F1 έως F12 μπαίνει πάνω πάνω, η σειρά του χρήστη ακριβώς από κάτω, πιο κοντά στα κύρια πλήκτρα επειδή χρησιμοποιείται συχνότερα. Η σειρά του χρήστη είναι λέξεις χωρισμένες με κενά, με τα ονόματα των extra keys του Termux (`ESC TAB CTRL ALT ~ | / - HOME END PGUP PGDN BKSP DEL INS`, χωρίς διάκριση πεζών), ώστε όποιος έχει ήδη ρυθμίσει το Termux να την αντιγράφει. Μόνο ονόματα πλήκτρων, modifiers και μεμονωμένοι χαρακτήρες· λέξεις όπως `git` αγνοούνται, γιατί θα ήταν snippets (v1.1, Pro). Έως 15 πλήκτρα, ίσου πλάτους. Αποθηκεύεται με το checkmark μέσα στο πεδίο, με Done ή με Enter, και όχι σε κάθε πάτημα, γιατί το πεδίο πληκτρολογείται με το ίδιο το πληκτρολόγιο. Η οθόνη ρυθμίσεων ελέγχει τις λέξεις ζωντανά με τον parser του `:ime` (`parseExtraRow`), μέσω του interface `ExtraRowInterpreter` του `:settings` που υλοποιεί το `:app`, ώστε να μην υπάρχει δεύτερη λίστα ονομάτων: δείχνει τα πλήκτρα που θα προκύψουν, κόκκινο σφάλμα για άγνωστες λέξεις και σημείωση πάνω από 15 πλήκτρα, και δεν αποθηκεύει άκυρο κείμενο.
- Άγνωστα πεδία απορρίπτονται, ώστε ένα typo όπως `widht` να μην περνά σιωπηλά. Απόφαση 2026-10-24: μένει έτσι· ξανασυζητιέται όταν έρθουν τα layouts του χρήστη στο v1.1. Κάθε σφάλμα έχει JSON path, π.χ. `$.rows[1].keys[3].key`, και αιτία.

### 4.4 Engine: από το άγγιγμα στο InputConnection

`KeyboardEngine` σε καθαρό Kotlin, χωρίς Android imports εκτός από `KeyEvent` σταθερές. Δέχεται pointer δεδομένα ως primitives, `onPointer(pointerId, x, y, action, timeMs, sink)`, τα αντιστοιχίζει σε πλήκτρο μέσω προϋπολογισμένης γεωμετρίας και εκπέμπει μέσω `InputSink` (`commitText(text)`, `sendKey(keyCode, metaState)`). Primitives και sink αντί για αντικείμενα `PointerEvent` και `InputCommand`, ώστε το `onTouchEvent` να μένει χωρίς allocations (βλ. 4.12 και 13). Οι εντολές που εκπέμπονται:

- `commitText(text)` για απλούς χαρακτήρες.
- `sendKey(keyCode, metaState)` για special keys και για οποιονδήποτε χαρακτήρα όταν είναι ενεργό Ctrl ή Alt.
- Backspace και Delete μέσω KeyEvents `DEL` και `FORWARD_DEL`, ώστε τα τερματικά να παίρνουν το πραγματικό πλήκτρο.
- Εναλλαγή layout και modifiers είναι εσωτερική κατάσταση του engine, όχι εντολή προς το `InputConnection`.

Ο `InputDispatcher` υλοποιεί το `InputSink` πάνω στο `InputConnection`: `commitText` για χαρακτήρες, ζεύγος `sendKeyEvent(down)` και `sendKeyEvent(up)` με `META_CTRL_ON | META_CTRL_LEFT_ON` (αντίστοιχα για Alt και Shift) για τα υπόλοιπα. Γιατί KeyEvents και όχι text: το Termux, ο vim και κάθε SSH client διαβάζουν raw key events για Ctrl+C, Esc και βελάκια. Το `commitText` δεν θα δούλευε ποτέ εκεί.

Fn layer: με ενεργό Fn η σειρά αριθμών στέλνει F1 έως F12, τα ↑ ↓ ← → στέλνουν PgUp, PgDn, Home, End και το Del στέλνει Ins, όπως στο Hacker's Keyboard· τα υπόλοιπα πλήκτρα γράφουν κανονικά και καταναλώνουν το one-shot Fn. Το view δείχνει τις ετικέτες του layer όσο είναι ενεργό. Η απόφαση αν ένα πάτημα χρησιμοποιεί το Fn παίρνεται στο DOWN και κρατιέται ανά pointer, ώστε ένα rollover που αφήνει το Fn λίγο πριν από το πλήκτρο να στέλνει ακόμα το F-key, και το repeat (και των PgUp/PgDn) να μένει στο ίδιο πλήκτρο. Το Fn δεν προσθέτει meta bits, άρα Ctrl+Fn+5 φτάνει ως Ctrl+F5. Modifiers, ένα state machine ανά modifier: `Off`, με tap γίνεται `OneShot`, με δεύτερο tap `Locked`, με τρίτο `Off`. Το `OneShot` καταναλώνεται στο επόμενο πλήκτρο. Το Fn ακολουθεί το ίδιο state machine εξ ορισμού· με τη ρύθμιση «Το Fn μένει ενεργό» (`fnToggles`, αίτημα του ketas στο #57, 2026-10-30) ένα tap το κλειδώνει και το επόμενο το σβήνει, όπως στο Hacker's Keyboard. Ctrl, Alt, Shift και Meta δεν αλλάζουν. Chording: όταν ένας pointer κρατά modifier και άλλος pointer πατά πλήκτρο, ο modifier εφαρμόζεται μόνο σε εκείνο το πάτημα. Η ανίχνευση γίνεται με tracking ανά pointer id.

Key repeat για Backspace, Delete, βελάκια και PgUp/PgDn, όχι για το space (ένα κράτημα στο space γράφει ένα κενό στο άφημα, όπως στο Hacker's Keyboard· ketas στο #57, 2026-10-29): αρχική καθυστέρηση 400 ms και μετά κάθε 50 ms. Ο χρόνος μπαίνει στο engine ως όρισμα (`onPointer(..., timeMs, sink)`, `onTick(timeMs, sink)`), και το engine δίνει την επόμενη προθεσμία με `nextDeadline`. Το view προγραμματίζει ένα προκατασκευασμένο `Runnable` με `postDelayed`. Όχι coroutine: το `:ime` δεν εξαρτάται από coroutines, και ένα `launch` σε κάθε πάτημα θα δέσμευε `Job` μέσα στο `onTouchEvent`. Το πρώτο repeat στα 400 ms μετρά ως το πάτημα, άρα ένα γρήγορο tap στέλνει ένα event στο άφημα και ένα κράτημα δεν στέλνει επιπλέον event στο άφημα. Ένας one-shot modifier ισχύει για όλα τα repeats και καθαρίζει στο άφημα. Swipe στο space: όταν ένας pointer που πάτησε το space μετακινηθεί οριζόντια κατά 8% του πλάτους του πληκτρολογίου (περίπου 1,2 πλήκτρα), όσο κι αν έχει κρατηθεί, το engine ζητά την επόμενη ή την προηγούμενη γλώσσα και δεν στέλνει space· ένα swipe αλλάζει γλώσσα μία φορά, και ένας one-shot modifier μένει για το επόμενο πλήκτρο. Η θέση του DOWN κρατιέται σε προκατασκευασμένο `FloatArray`, χωρίς allocations. Άφημα λίγο έξω από το πλήκτρο (ως 0,4 της μικρότερης πλευράς του) γράφει το πλήκτρο που πατήθηκε· άφημα πιο μακριά γράφει το πλήκτρο κάτω από το δάχτυλο, όπως στο Hacker's Keyboard (ketas στο #57, 2026-10-28), εκτός αν το ένα από τα δύο είναι modifier ή το δάχτυλο φύγει από το πληκτρολόγιο. Ολίσθηση εκτός πλήκτρου ή πάτημα άλλου πλήκτρου σταματά το repeat· πάτημα modifier όχι, ώστε Shift στη μέση ενός ← να επεκτείνει την επιλογή. Long-press στο system timeout (`ViewConfiguration.getLongPressTimeout()`, 400 ms εξ ορισμού, 500 ms στο S23 Ultra), όχι σε σταθερά 300 ms: ένα πιο σύντομο όριο βγάζει τυχαία κεφαλαία σε όποιον πληκτρολογεί αργά, και ο χρήστης το ρυθμίζει ήδη στις ρυθμίσεις προσβασιμότητας (Touch & hold delay). Ίδιος μηχανισμός προθεσμίας με το repeat: αν το πλήκτρο έχει alternates, ανοίγει μία σειρά από cells πάνω από το πλήκτρο (όχι πλέγμα, μέσα στο ίδιο view), ολίσθηση επιλέγει, άφημα στέλνει την επιλογή, ολίσθηση κάτω από το πλήκτρο ακυρώνει. Αν δεν έχει, στέλνει τον shifted χαρακτήρα (κράτημα `a` δίνει `A`), ώστε κάθε πλήκτρο να κάνει κάτι χρήσιμο χωρίς δεδομένα. Τα γράμματα με τόνους και τα σύμβολα έχουν alternates (EN: à á â… στο a, è é ê… € στο e, ñ, ç, ß, ł, õ, š, ž κ.λπ., ώστε να καλύπτονται οι δυτικο- και κεντροευρωπαϊκές γλώσσες· EN και EL: … · στην τελεία, ’ ‘ ´ στο ', « ‹ » › στις αγκύλες, „ ‚ στο κόμμα, ÷ ¿ στο /). Πρώτο alternate είναι πάντα η shifted μορφή (το κεφαλαίο στα γράμματα, ? στο /, > στην τελεία), ώστε το απλό κράτημα να δίνει ό,τι δίνει και το Shift και το σύρσιμο τα υπόλοιπα (αίτημα του ketas στο #875· για τα σύμβολα απόφαση του Dimitris, 2026-10-25). Στη συμπαγή διάταξη η πάνω σειρά γραμμάτων έχει πρώτο το ψηφίο. Το popup τοποθετείται όσο πιο κοντά στο πλήκτρο χωράει· το cell κάτω από το δάχτυλο παίρνει το πρώτο alternate και τα υπόλοιπα απλώνονται προς την πλευρά με περισσότερο χώρο και μετά τυλίγονται στην άλλη, ώστε το απλό κράτημα να δίνει πάντα το πρώτο ακόμη κι όταν το popup πιάνει όλο το πλάτος. Hint (μικρό, πάνω δεξιά, όπως στο Hacker's Keyboard): ό,τι προσθέτει το κράτημα πέρα από το Shift· στα γράμματα κανένα hint εκτός αν το πρώτο alternate δεν είναι το κεφαλαίο (π.χ. τα ψηφία της συμπαγούς), στα σύμβολα το επόμενο alternate μετά τη shifted μορφή (÷ στο /), και στη σειρά αριθμών F1 έως F12. Οι θέσεις των hints υπολογίζονται μαζί με τη γεωμετρία, όχι στο `onDraw`. Πάτημα άλλου πλήκτρου πριν από το timeout ακυρώνει το long-press, ώστε το γρήγορο rollover να μη βγάζει κεφαλαία. Το popup σχεδιάζεται σε διαφανή ζώνη ύψους μίας σειράς πάνω από τα πλήκτρα· το `onComputeInsets` δηλώνει content, visible και touchable μόνο την περιοχή των πλήκτρων, άρα τα αγγίγματα στη ζώνη περνούν στην εφαρμογή. Κεφαλαίο στην αρχή πρότασης μόνο στη συμπαγή διάταξη και μόνο σε πεδία κειμένου (`TYPE_CLASS_TEXT`), ανοιχτό εξ ορισμού, μέσω `InputConnection.getCursorCapsMode` (σέβεται τα `TYPE_TEXT_FLAG_CAP_*` του πεδίου) και ενός αυτόματου one-shot Shift που κεφαλαιοποιεί μόνο γράμματα και δεν μπαίνει ποτέ στο meta state των KeyEvents· ένα dead key το κρατά μέχρι το γράμμα (΄ α → Ά).

### 4.5 InputMethodService: lifecycle, insets, EditorInfo

- `onCreate`: δημιουργεί `serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)`, ξεκινά τη συλλογή ρυθμίσεων από το DataStore και φορτώνει το τρέχον layout σε `Dispatchers.Default`. Το service δημιουργείται πριν εμφανιστεί το view, άρα στις περισσότερες περιπτώσεις το snapshot είναι έτοιμο στο `onCreateInputView`. Αν δεν είναι, το view ξεκινά με defaults και ενημερώνεται.
- `onCreateInputView`: επιστρέφει το `KeyboardView`, φθηνό, χωρίς I/O.
- `onStartInputView(info, restarting)`: διαβάζει `EditorInfo.packageName`, `inputType` και `imeOptions`, επιλέγει προφίλ και layout. Σε password variation απενεργοποιεί την προεπισκόπηση πλήκτρου. Σε number/phone/date πεδίο η συμπαγής διάταξη ανοίγει στη σελίδα `?123`· ξεχωριστό αριθμητικό layout δεν υπάρχει ακόμη.
- `onComputeInsets`: ορίζει `contentTopInsets` και `visibleTopInsets` ώστε το window να καλύπτει την περιοχή προεπισκόπησης αλλά η touchable region να είναι μόνο τα πλήκτρα.
- Insets για gesture navigation: ο χειρισμός γίνεται με `ViewCompat.setOnApplyWindowInsetsListener` και `WindowInsetsCompat`, προσθέτοντας ως padding το κάτω, το αριστερό και το δεξί inset από navigation bar, tappable element και display cutout (στο landscape η navigation bar με 3 κουμπιά είναι στο πλάι), χωρίς deprecated `systemUiVisibility` APIs.
- `onEvaluateFullscreenMode` επιστρέφει πάντα false: κανένα extract UI στο landscape, η εφαρμογή μένει ορατή πάνω από το πληκτρολόγιο. Το ύψος σειράς είναι ρύθμιση ανά προσανατολισμό: `rowHeightDp` στο portrait (default 48dp, εύρος 36 έως 64dp) και `landscapeRowHeightDp` στο landscape (default 34dp, εύρος 26 έως 56dp). Με το default του landscape το πληκτρολόγιο πιάνει περίπου 45% της οθόνης.
- Enter: εκτός τερματικών τιμά το `imeOptions` action (Go, Search, Send, Next, Done) μέσω `performEditorAction`, εκτός αν το πεδίο ορίζει `IME_FLAG_NO_ENTER_ACTION` (το default του TextView για multi-line, όπου βάζει νέα γραμμή· ένα multi-line πεδίο που ζητά ρητά Send παίρνει Send, όπως στο AOSP, απόφαση 2026-10-25) ή είναι ενεργό Ctrl, Alt ή Meta (το Shift δεν μετράει). Αν το `performEditorAction` αποτύχει, στέλνει το πλήκτρο. Στα τερματικά και σε raw πεδία (`TYPE_NULL`) στέλνει πάντα raw `KEYCODE_ENTER` (`dispatch/TypingRules.kt`).
- Μέγεθος οθόνης: επαναϋπολογισμός γεωμετρίας σε `onSizeChanged` και `onConfigurationChanged`. Πλάτος κάτω από 600dp είναι phone mode, 600dp και πάνω επιτρέπει split mode. Το Jetpack WindowManager για fold posture μένει για το v1.1.
- `onDestroy`: ακυρώνει το `serviceScope`.
- Lifecycle API: `ServiceLifecycleDispatcher` από το `lifecycle-service` ώστε το service να εκθέτει `LifecycleOwner` όπου χρειάζεται.

### 4.6 Προφίλ ανά εφαρμογή

Σχεδιασμός στο `docs/specs/2026-10-25-per-app-profiles-design.md`, υλοποίηση στο `docs/plans/2026-10-25-per-app-profiles.md` (αποφάσεις του Dimitris, 2026-10-25). Σε κάθε πεδίο το `BuckyImeService.onStartInputView` καλεί την καθαρή `profileFor` (`:app`, `profile/`) και διαλέγει `FULL`, `COMPACT` ή `HAND_OFF`:

- Οι εφαρμογές τερματικού (`com.termux`, `org.connectbot`, `com.sonelli.juicessh`, `com.server.auditor.ssh.client`), η ίδια η εφαρμογή του Bucky και ένα full που ζητήθηκε με κράτημα του ⌨ παίρνουν πάντα την πλήρη διάταξη, με raw Enter μόνο στα τερματικά.
- Οι άλλες εφαρμογές ακολουθούν τη ρύθμιση «Εκτός εφαρμογών τερματικού» (ενότητα «Εφαρμογές»): Συμπαγής διάταξη (default, και για όσους αναβαθμίζουν από alpha), Πλήρης διάταξη, ή Εναλλαγή στο άλλο μου πληκτρολόγιο.
- Η εναλλαγή γίνεται με `switchToPreviousInputMethod` (API 28+, μόνο όταν `shouldOfferSwitchingToNextInputMethod`). Το Android δεν μπορεί να φέρει πίσω το Bucky όταν ξαναμπείς σε τερματικό, γιατί τότε δεν τρέχει· γι' αυτό η ρύθμιση το λέει ρητά. Σε API 26–27, ή χωρίς άλλο πληκτρολόγιο, μένει η συμπαγής διάταξη και το ⌨ γίνεται πλήκτρο που δείχνει την πλήρη διάταξη (ώστε η διαφυγή για SSH εφαρμογές εκτός λίστας να μένει). Το `method.xml` δηλώνει `supportsSwitchingToNextInputMethod`, αλλιώς το σύστημα δεν προσφέρει εναλλαγή προς πληκτρολόγια που το δηλώνουν (π.χ. Gboard). Αν ο χρήστης διαλέξει ξανά το Bucky με το χέρι στην εφαρμογή από την οποία μόλις έγινε η εναλλαγή, μένει (συμπαγής) αντί να ξαναφύγει. Η εναλλαγή καταστρέφει το service, γι' αυτό η cache των layouts, η γλώσσα και η εφαρμογή της τελευταίας εναλλαγής ζουν στο process-wide `ImeSession` (απόφαση 2026-10-25).
- Η ρύθμιση «Στυλ πλήρους διάταξης» (Κεντραρισμένη ή Hacker's Keyboard κλασική, 2026-10-27) ισχύει όπου εμφανίζεται η πλήρης διάταξη: σε τερματικά, στο προφίλ Full και στο κράτημα του ⌨.
- Το κράτημα του ⌨ δείχνει την πλήρη διάταξη μέχρι την επόμενη αλλαγή εφαρμογής (δωρεάν). Η μόνιμη επιλογή ανά εφαρμογή μένει Pro, μαζί με τα custom προφίλ στο v1.1. Δεν χρειάζεται λίστα εγκατεστημένων εφαρμογών, άρα ούτε `<queries>` ούτε `QUERY_ALL_PACKAGES`.

Η συμπαγής διάταξη μοιάζει με κανονικό πληκτρολόγιο κινητού (`compact_en_us`, `compact_el`, και οι κοινές σελίδες `compact_symbols` `?123` και `compact_symbols_more` `=\<`). Η πάνω σειρά γραμμάτων δίνει 1 έως 0 στο κράτημα· ένα space σε σελίδα συμβόλων γυρίζει στα γράμματα (`"then"`). Οι 4 σειρές της μοιράζονται το ύψος των 5 κύριων σειρών της πλήρους, άρα τα πλήκτρα είναι περίπου 25% ψηλότερα, χωρίς νέα ρύθμιση. Η σειρά F1 έως F12 και η σειρά του χρήστη εμφανίζονται μόνο στην πλήρη. Στη συμπαγή: κεφαλαίο στην αρχή πρότασης (`getCursorCapsMode` από το `onUpdateSelection`, μέσω `KeyboardEngine.setAutoShift`, ώστε το engine να μένει χωρίς Android) και διπλό space για τελεία (στον `InputDispatcher`), και οι δύο με διακόπτη, ποτέ σε τερματικό, password ή πεδίο `NO_SUGGESTIONS`.

### 4.7 Settings και onboarding σε Compose

Material 3 με dynamic color, MVVM με `StateFlow`. Η αρχική οθόνη έχει ενότητες Γλώσσες, Μέγεθος (ύψος πλήκτρων σε portrait και landscape, μέγεθος γραμμάτων 80 έως 200%· πάνω από το 100% οι μονοί χαρακτήρες μεγαλώνουν αφού χωρέσουν στο πλήκτρο, ώστε να μπορούν να βγουν λίγο έξω από αυτό όπως στο Hacker's Keyboard, ενώ οι λέξεις μένουν μέσα, και οι ετικέτες φτάνουν ως το 80% του ύψους της σειράς· αίτημα του ketas, 2026-10-27), Απόκριση πλήκτρων (προεπισκόπηση, δόνηση, Fn που μένει ενεργό ως το επόμενο πάτημα), Επιπλέον σειρές (F1 έως F12, σειρά του χρήστη με checkmark αποθήκευσης· κενό πεδίο σβήνει τη σειρά) και Εφαρμογές (βλ. 4.6). Αποτυχία αποθήκευσης δείχνει toast. Οθόνες v1: Onboarding (ενεργοποίηση στο `Settings.ACTION_INPUT_METHOD_SETTINGS`, επιλογή με `InputMethodManager.showInputMethodPicker()`, δοκιμή σε πεδίο κειμένου), Αρχική, Layouts, Εμφάνιση, Pro. Η κατάσταση ενεργοποίησης διαβάζεται από `Settings.Secure.ENABLED_INPUT_METHODS` και `Settings.Secure.DEFAULT_INPUT_METHOD` με lifecycle-aware polling όσο η οθόνη onboarding είναι resumed. Navigation: Navigation 3 αν είναι stable τη στιγμή της υλοποίησης, αλλιώς sealed back stack σε Compose state, αφού μιλάμε για πέντε οθόνες. Το κείμενο του onboarding απαντά ρητά στην προειδοποίηση του συστήματος ότι ένα IME μπορεί να συλλέγει ό,τι πληκτρολογείς: καμία άδεια INTERNET, ανοιχτός κώδικας, σύνδεσμος στο repo.

### 4.8 Persistence

Οι ρυθμίσεις ζουν στο `:settings` (`keyboard/domain/model/KeyboardSettings`, `KeyboardSettingsRepository` και `KeyboardSettingsRepositoryImpl` στο `keyboard/data`), όχι στο `:ime`: το `:settings` δεν επιτρέπεται να εξαρτάται από το `:ime`, και το `:ime` μένει χωρίς persistence. Το `BuckyImeService` στο `:app` συλλέγει το `Flow`, περνά απλές τιμές στο `KeyboardView` (γλώσσες, ύψος σειράς ανά προσανατολισμό, μέγεθος ετικετών, preview, haptics, Fn ως toggle, σειρά F1 έως F12 από το `fKeyRow` και σειρά χρήστη από το `extraRow`, που προστίθενται σε κάθε layout με `Layout.withExtraRows`) και σε κάθε αλλαγή ξαναφτιάχνει το view με `setInputView`. Οι default γλώσσες είναι EN, και EN με EL όταν η γλώσσα του συστήματος είναι ελληνικά. Με μία γλώσσα το swipe στο space είναι κλειστό, και ένα custom layout που έχει πλήκτρο 🌐 το χάνει κατά τη φόρτωση (`withoutLanguageKey`, το πλάτος του πάει στο φαρδύτερο πλήκτρο). DataStore με custom `Serializer<T>` που χρησιμοποιεί kotlinx.serialization JSON. Έτσι έχουμε typed, migratable ρυθμίσεις χωρίς την protobuf toolchain. Το `@Serializable` μένει πάνω στο domain model `KeyboardSettings`, χωρίς ξεχωριστό data model και mapper (απόφαση 2026-10-24): τα πεδία είναι απλές τιμές και ένα δεύτερο αντίγραφο θα ήταν μόνο boilerplate. `ReplaceFileCorruptionHandler` επιστρέφει defaults σε `CorruptionException`. Τα layouts αποθηκεύονται ως JSON αρχεία σε `filesDir/layouts` μέσω `AtomicFile`. Όχι Room: δεν υπάρχουν σχέσεις, και η αρχικοποίηση βάσης στο ξεκίνημα του IME προσθέτει latency χωρίς αντάλλαγμα. Οι αλλαγές ρυθμίσεων φτάνουν στο IME μέσω του `Flow` του DataStore στο ίδιο process.

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

`ExploreByTouchHelper` (androidx.customview) εκθέτει κάθε πλήκτρο ως virtual view με content description, ώστε το TalkBack να δουλεύει με explore-by-touch. `announceForAccessibility` στην εναλλαγή layout και κατάστασης modifier. Ύψος πλήκτρου 48dp στο default του portrait και 34dp στο default του landscape· ο χρήστης το αλλάζει με τις ρυθμίσεις `rowHeightDp` (36 έως 64dp) και `landscapeRowHeightDp` (26 έως 56dp). Μέγεθος ετικετών `labelScalePercent` 80 έως 140% σε βήματα 10 (αίτημα του ketas στο #875, 2026-10-25): πολλαπλασιάζει τα βασικά μεγέθη, αλλά το fit στο πλάτος του πλήκτρου και στο 55% του ύψους της σειράς μένει, άρα τα γράμματα μεγαλώνουν μόνο όσο χωρούν (σε portrait με 15 πλήκτρα σχεδόν καθόλου, με ψηλότερα πλήκτρα ή σε landscape περισσότερο). Κάθε κατηγορία ετικετών (ένας χαρακτήρας, λέξη, Fn) παίρνει ένα κοινό μέγεθος, το μικρότερο που χωρά, ώστε ένα πλήκτρο με φαρδύ shifted (5 και %) να μη μικραίνει μόνο του. Τα hints μένουν σε σταθερό μέγεθος. High-contrast θέμα στο v1.1.

### 4.14 Privacy και permissions

- Καμία άδεια INTERNET. Καμία `QUERY_ALL_PACKAGES`. Το service δηλώνεται με `android:permission="android.permission.BIND_INPUT_METHOD"` και `android:exported="true"`, όπως απαιτεί το πλαίσιο IME.
- Σε password fields: καμία προεπισκόπηση, καμία αποθήκευση. Δεν υπάρχει ιστορικό πληκτρολόγησης πουθενά στην εφαρμογή.
- Data Safety: δεν συλλέγονται και δεν κοινοποιούνται δεδομένα. Το `play` flavor περιλαμβάνει μόνο Play Billing και In-App Review, που επεξεργάζονται δεδομένα υπό την πολιτική της Google και δηλώνονται ανάλογα.

### 4.15 Build, flavors, CI

- Kotlin 2.x με K2, AGP τελευταίο stable, version catalog, KSP για Hilt, Compose compiler plugin του Kotlin.
- Flavor dimension `distribution` με `play` και `foss`, ίδιο applicationId.
- R8 ενεργό και στα δύο flavors. Οι consumer rules της kotlinx.serialization καλύπτουν το serialization.
- Signing: Play App Signing με upload key για το `play`, δικό μας release key για το `foss` (GitHub Releases, IzzyOnDroid). Το F-Droid χτίζει και υπογράφει μόνο του από το `foss` flavor.
- GitHub Actions: σε κάθε PR build και στα δύο flavors, Android Lint, unit tests. Σε tag: AAB για Play internal track και APK για GitHub Release με checksums, upload στο Play με Gradle Play Publisher ή fastlane supply. Μέχρι να γίνει αυτό, το `foss` release APK χτίζεται και υπογράφεται τοπικά με `assembleFossRelease`: τα στοιχεία του κλειδιού διαβάζονται από Gradle properties ή από το `local.properties` (εκτός git), και το APK ανεβαίνει χειροκίνητα με `.sha256`.
- Δομή `fastlane/metadata/android/` για περιγραφές και screenshots, που διαβάζουν και το F-Droid και το IzzyOnDroid.

### 4.16 Εξαρτήσεις Gradle

Pin τις τελευταίες stable εκδόσεις από Google Maven και Maven Central την ημέρα του setup. Δεν γράφω αριθμούς εκδόσεων εδώ για να μην παλιώσουν.

Plugins: `com.android.application`, `com.android.library`, `org.jetbrains.kotlin.plugin.compose`, `org.jetbrains.kotlin.plugin.serialization`, `com.google.devtools.ksp`, `com.google.dagger.hilt.android`, και για τα εργαλεία `org.jetbrains.kotlin.jvm` (`:detekt-rules`), `com.diffplug.spotless`, `io.gitlab.arturbosch.detekt`. Το AGP 9 έχει ενσωματωμένο Kotlin, άρα όχι `org.jetbrains.kotlin.android`.

| Σκοπός | Artifact | Module |
|---|---|---|
| Core KTX | `androidx.core:core-ktx` | :app, :ime, :settings |
| Lifecycle σε Service | `androidx.lifecycle:lifecycle-service` (όχι ακόμη· το service έχει δικό του `CoroutineScope`) | :app |
| Lifecycle runtime Compose | `androidx.lifecycle:lifecycle-runtime-compose` (`collectAsStateWithLifecycle`· ViewModel μέσω `by viewModels()` στο Activity, χωρίς `lifecycle-viewmodel-compose`) | :settings |
| DataStore (custom serializer) | `androidx.datastore:datastore` | :settings |
| JSON | `org.jetbrains.kotlinx:kotlinx-serialization-json` (και το plugin `org.jetbrains.kotlin.plugin.serialization` στο :settings) | :ime, :settings |
| Coroutines | `org.jetbrains.kotlinx:kotlinx-coroutines-android` στο :app, `kotlinx-coroutines-core` ως `api` στο :billing, transitively μέσω DataStore στο :settings | :app, :billing, :settings |
| Compose | `androidx.compose:compose-bom`, `androidx.compose.material3:material3`, `androidx.compose.ui:ui`, `androidx.compose.ui:ui-tooling-preview` (και `ui-tooling` στο debug), `androidx.activity:activity-compose` | :settings |
| Navigation (αν stable, όχι ακόμη) | `androidx.navigation3:navigation3-runtime`, `androidx.navigation3:navigation3-ui` | :settings |
| Hilt | `com.google.dagger:hilt-android`, `com.google.dagger:hilt-android-compiler` (ksp)· `androidx.hilt:hilt-navigation-compose` μόνο αν προστεθεί Navigation | :app, :settings |
| Accessibility helper (εβδομάδα 9) | `androidx.customview:customview` | :ime |
| Logging | `com.jakewharton.timber:timber` | :app, :ime |
| Play Billing (εβδομάδα 10) | `com.android.billingclient:billing-ktx` | :billing-play |
| In-App Review (εβδομάδα 10) | `com.google.android.play:review-ktx` | :app (play) |
| Tests και εργαλεία | `junit:junit`, `com.lemonappdev:konsist` (μόνο :app), `io.gitlab.arturbosch.detekt:detekt-api` και `detekt-test` (:detekt-rules) | όλα |
| Baseline Profiles (v1.1) | `androidx.profileinstaller:profileinstaller` | :app |

## 5. Χρονοδιάγραμμα: 14 εβδομάδες, περίπου 17 ώρες η καθεμία

| Εβδ. | Φάση | Παραδοτέα | Definition of done |
|---|---|---|---|
| 1 | Επαλήθευση | Καθημερινή χρήση Unexpected Keyboard και HeliBoard σε Termux, ανάγνωση #875, #939 και Reddit, λίστα top-10 κενών, επιλογή ονόματος, landing page με waitlist, post στο #875 και στο r/termux | Λίστα κενών γραμμένη, σελίδα live, posts δημοσιευμένα |
| 2 | Spike και go/no-go | Repo, modules, `InputMethodService` skeleton, Canvas view με στατικό layout που πληκτρολογεί σε Termux, μέτρηση latency | Ctrl+C και Esc δουλεύουν στο Termux, Gate 0 αποφασισμένο |
| 3 | Engine | Layout model και parser, γεωμετρία, touch σε πλήκτρο, `commitText` και `sendKeyEvent`, PC Full EN | Πληκτρολόγηση σε Termux και σε browser χωρίς λάθη χαρακτήρων |
| 4 | Engine | Modifiers state machine, chording, key repeat, long-press alternates, Backspace/Delete ως KeyEvents | Ctrl+Alt+Del, Ctrl+Shift+βελάκι και long-press σωστά σε Termux και σε Google Docs |
| 5 | Engine | Προεπισκόπηση μέσα στο view, `onComputeInsets`, insets gesture navigation, haptics, landscape με fallback πλάτους και padding | Μηδενικές allocations στο draw path, σωστή συμπεριφορά σε gesture και 3-button navigation |
| 6 | Layouts και θέμα | 4-row, compact, αριθμητικό, EL, Dvorak, Colemak, UK, σειρά F1 έως F12, επιπλέον σειρά χρήστη, Material You light/dark | Όλα τα layouts φορτώνουν από JSON, εναλλαγή EL/EN με πλήκτρο γλώσσας |
| 7 | Settings | Compose settings app, DataStore, ροή ρυθμίσεων στο IME, επιλογή layouts, ύψος, number row | Κάθε ρύθμιση εφαρμόζεται ζωντανά στο IME χωρίς restart |
| 8 | Onboarding και προφίλ | Onboarding 3 βημάτων, built-in προφίλ ανά εφαρμογή, password και number inputType handling, split mode 600dp+ | Νέος χρήστης ενεργοποιεί και επιλέγει το IME σε κάτω από 60 δευτερόλεπτα |
| 9 | Ποιότητα | ExploreByTouchHelper, crash-proofing με fallback layout, performance pass, Logcat latency metrics | TalkBack διαβάζει κάθε πλήκτρο, κανένα crash σε fuzz χειροκίνητης χρήσης 30 λεπτών |
| 10 | Flavors και billing | `play`/`foss`, `:billing-*`, Pro screen, entitlement cache, In-App Review hook, CI με GitHub Actions | Αγορά και επαναφορά δουλεύουν σε Play internal testing, το `foss` build είναι unlocked |
| 11 | Beta | Internal testing, μετά closed testing με 30 με 50 testers από το waitlist και το #875, device matrix (Samsung, Pixel, Xiaomi, ένα foldable) | Λίστα bugs ταξινομημένη, blockers μηδέν |
| 12 | Beta και store | Διορθώσεις, screenshots, listing, privacy policy, Data Safety, trader declaration, IzzyOnDroid submission, F-Droid merge request | Listing εγκεκριμένο στο Play, APK στο IzzyOnDroid |
| 13 | Launch | Production σε staged rollout 20%, 50%, 100% μέσα σε 5 ημέρες, ανακοινώσεις (βλ. 7.5) | v1.0 στο 100%, crash-free πάνω από 99.5% στα Play vitals |
| 14 | Buffer | Ό,τι γλίστρησε, πρώτες διορθώσεις από reviews | Backlog για v1.1 γραμμένο |

Κατάσταση στις 2026-10-25: οι εβδομάδες 1 έως 5 έχουν κλείσει (μένει το χειροκίνητο fallback πλάτους για OEM builds, hackerskeyboard#957). Από την εβδομάδα 6 έχουν γίνει το EL με dead key τόνου, η εναλλαγή γλώσσας με swipe στο space (το πλήκτρο 🌐 έφυγε από τα built-in layouts), η σειρά F1 έως F12, η σειρά του χρήστη, το Fn layer, τα long-press alternates με hints, η συμπαγής διάταξη EN και EL με σελίδες `?123` και `=\<`, και το ET (2026-10-26, βλ. 4.3)· λείπουν UK, Dvorak, Colemak, αριθμητικό και Material You. Η εβδομάδα 7 έχει κλείσει (ρυθμίσεις σε DataStore με ζωντανή εφαρμογή, γλώσσες, ύψος σε portrait και landscape, μέγεθος γραμμάτων, προεπισκόπηση, haptics). Από την εβδομάδα 8 έχουν γίνει τα built-in προφίλ ανά εφαρμογή και η ενότητα Εφαρμογές (βλ. 4.6)· λείπουν το split mode και το onboarding 3 βημάτων. Alphas στο GitHub: `v0.1.0-alpha1` και `v0.1.0-alpha2` (2026-10-24), `v0.1.0-alpha3` (2026-10-25, προφίλ ανά εφαρμογή), `v0.1.0-alpha4` (2026-10-26, θέσεις Hacker's Keyboard, Estonian, διόρθωση insets), `v0.1.0-alpha5` (2026-10-27, κλασική διάταξη Hacker's Keyboard, ⇧ ⇥, πιο σκούρα special keys, γράμματα ως 200%), `v0.1.0-alpha6` (2026-10-27, Finnish, πλήκτρο ⚙, γρήγορα αγγίγματα που γλιστρούν, shifted σύμβολο πρώτο στα popups, γλώσσα στο space), `v0.1.0-alpha7` (2026-10-28, χωρίς κενή λωρίδα ανάμεσα στα πλήκτρα και τη μπάρα πλοήγησης στο Android 13 και μετά από περιστροφή), `v0.1.0-alpha8` (2026-10-28, ίδιο μέγεθος γραμμάτων στη φινλανδική, άφημα σε άλλο πλήκτρο όπως στο Hacker's Keyboard, γλώσσα στο space της σελίδας Fn, long-press στα dead keys), `v0.1.0-alpha9` (2026-10-29, ξεχωριστό popup του long-press, μεγαλύτερη αντίθεση στα special keys και στο space, προεπισκόπηση σε όλα τα πλήκτρα εκτός από τους modifiers, ␣ κάτω από τη γλώσσα), `v0.1.0-alpha10` (2026-10-30, κενό ανάμεσα στην επιλογή του popup και τη γραμμή accent, κεντραρισμένη ετικέτα στο space, χωρίς repeat στο space), `v0.1.0-alpha11` (2026-10-31, μεγαλύτερες ετικέτες-λέξεις στην προεπισκόπηση, ρύθμιση για Fn που μένει ενεργό).

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
- Κριτήριο διακοπής: το αριθμητικό κριτήριο του Gate 0 στην ενότητα 10 (stop κάτω από 25 εγγραφές και με λιγότερες από 3 ουσιαστικές απαντήσεις).

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

Αλλαγή στις 2026-10-24: πριν από το internal testing βγαίνουν alpha builds του `foss` ως pre-releases στο GitHub (πρώτο το `v0.1.0-alpha1`), ώστε να έρθει πραγματικό feedback από το #875 και το r/termux πριν από το Gate 0 και όχι στην εβδομάδα 11. Τα pre-releases δεν γίνονται «Latest», άρα όποιος θέλει σταθερή έκδοση δεν τα βρίσκει κατά λάθος. Κάθε νέο alpha ανεβάζει το `versionCode` και υπογράφεται με το ίδιο foss release key. Το waitlist μένει για το Play beta, και το site δεν δείχνει το alpha πριν από το Gate 0, ώστε το gate να μετρά αυτό που σχεδιάστηκε να μετρά.

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

- Gate 0, δύο εβδομάδες μετά τα posts (2 Νοεμβρίου 2026), με προϋπόθεση το spike που αποδεικνύει Ctrl+C και Esc σε Termux (έγινε):
  - Continue αν ισχύει ένα από τα δύο: (α) τουλάχιστον 100 εγγραφές στο waitlist, ή (β) τουλάχιστον 40 εγγραφές και τουλάχιστον 8 διαφορετικά άτομα με συγκεκριμένο αίτημα feature ή προσφορά για beta test και τουλάχιστον 25 stars στο repo.
  - Παράταση μίας εβδομάδας, μία φορά μόνο: 25 έως 39 εγγραφές, ή όταν ισχύουν δύο από τα τρία κριτήρια του (β).
  - Stop: κάτω από 25 εγγραφές και λιγότερες από 3 ουσιαστικές απαντήσεις. Κόστος δύο εβδομάδων.
  - Μετράνε μόνο άτομα εκτός από τον Dimitris. Το Gate 0 δεν ελέγχει διάθεση πληρωμής· αυτό το κάνει το Gate 2.
- Gate 1, 8 εβδομάδες μετά το launch: βαθμολογία 4,3 και πάνω, crash-free 99,5% και πάνω, τουλάχιστον 300 Pro πωλήσεις. Αν περάσει, ξεκινά το v1.2 με prediction. Αν όχι, v1.1 ολοκληρώνεται και το project μπαίνει σε maintenance mode.
- Gate 2, μήνας 9: καθαρό έσοδο προς ώρες. Αν είναι κάτω από 15 € την ώρα, maintenance mode μόνιμα και ο χρόνος πάει στο επόμενο προϊόν. Το πληκτρολόγιο συνεχίζει να πουλά και να συντηρείται.

Maintenance mode σημαίνει: ενημέρωση targetSdk κάθε χρόνο, διορθώσεις crash, merge PRs από την κοινότητα, τίποτα άλλο.

## 11. Backlog (parking lot)

- On-device prediction και autocorrect με λεξικά ανά γλώσσα (Pro, v1.2, μόνο μετά το Gate 1).
- Macros/snippets key: `cd ~/projects`, `git status`, custom εντολές (Pro, v1.1).
- Mic key που εναλλάσσει στο voice IME του συστήματος.
- Clipboard history αποκλειστικά στη μνήμη, opt-in.
- Emoji μέσω alternates ή system picker.
- Σελίδα επιλογής συμβόλων με τους πιο συχνούς ειδικούς χαρακτήρες (αίτημα του ketas στο #57, 2026-10-30): υποψήφια μετά το Gate 0. Μέχρι τότε όποιος χαρακτήρας λείπει μπαίνει ως long-press alternate (απόφαση 2026-10-31).
- Jetpack WindowManager για fold posture.
- Themes marketplace: όχι, ποτέ.
- Gesture typing: όχι, ποτέ.
- Physical keyboard remapping: διαφορετικό προϊόν, όχι.
- iOS: όχι.

## 12. Ανοιχτές ερωτήσεις

1. Όνομα και domain: αποφασίστηκε Bucky / `buckykeys.app` (18/10/2026). Μένει ο έλεγχος TMview και USPTO.
2. Ποιες γλώσσες πέρα από EN και EL στο v1; Κάθε γλώσσα είναι data, αλλά χρειάζεται native tester. Ζητήθηκε Estonian από τον ketas στο #875, που διάλεξε την τυπική εσθονική διάταξη και θα τη δοκιμάσει· έγινε στις 2026-10-26.
3. Αγορά μεταχειρισμένων συσκευών για το device matrix (ένα Samsung μεσαίας κατηγορίας, ένα Xiaomi, ένα foldable) ή αποκλειστική εξάρτηση από beta testers;
4. Hosting για landing page και privacy policy: αποφασίστηκε Vercel (buckykeys.app, με το waitlist στο Upstash Redis).
5. Επαγγελματική διεύθυνση για το trader declaration.

## 13. Συμβάσεις υλοποίησης (για το Claude Code)

- Αυτό το αρχείο είναι το τρέχον πλάνο για scope και αρχιτεκτονική, όχι συμβόλαιο. Οι αποφάσεις του αμφισβητούνται και βελτιώνονται όσο δουλεύουμε· αλλαγές σε αρχιτεκτονική ή scope συμφωνούνται πρώτα. Μόλις συμφωνηθεί μια αλλαγή scope, ένα νέο module ή μια διαφορετική προσέγγιση, το PLAN.md ενημερώνεται στο ίδιο commit. Αναφορά σε αυτό από το CLAUDE.md.
- Kotlin official code style. Explicit API mode στα library modules. Κανένα `!!`. Nullable τιμές λύνονται στα όρια (InputConnection, EditorInfo, parsing), όχι στο εσωτερικό του engine.
- Κανένα deprecated API. Έλεγχος `@Deprecated` σε κάθε κλήση πλαισίου, προτίμηση σε `WindowInsetsCompat`, `ViewCompat`, `ServiceLifecycleDispatcher`.
- Το `:ime` δεν εισάγει Compose, billing ή Play Services. Το `KeyboardEngine` δεν έχει Android imports εκτός από σταθερές `KeyEvent`.
- Structured concurrency μόνο: κάθε coroutine σε scope με ιδιοκτήτη, καμία `GlobalScope`, κανένα `runBlocking` σε production κώδικα.
- Σφάλματα ως sealed types ή `Result`, ποτέ exceptions ως control flow πέρα από τα όρια.
- Καμία allocation σε `onDraw` και `onTouchEvent`. Κάθε αλλαγή στο `KeyboardView` συνοδεύεται από χειροκίνητο έλεγχο latency σε συσκευή και σημείωση στο PR.
- Κανένα string hardcoded στο UI, όλα σε resources, EL και EN από το v1.
- Κώδικας χωρίς σχόλια και χωρίς KDoc, εκτός από ένα μονόγραμμο «γιατί» όταν δεν είναι προφανές. Η τεκμηρίωση ζει σε αυτό το αρχείο.
- Commits με DCO sign-off, conventional commits.
