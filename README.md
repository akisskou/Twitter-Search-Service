# Twitter-Search-Service

Οδηγίες για τη λειτουργία του service αναζήτησης στο Twitter:
- Μετά το ανέβασμα του SMA_Adapter στον server, η αρχικοποίηση και εκκίνηση του service γίνεται με τον ίδιο ακριβώς τρόπο όπως και πριν. Πρώτα στέλνουμε post στο url http://ponte.grid.ece.ntua.gr:8280/SMA_Adapter/TwitterServlet με τα credentials στο body, έπειτα get στο ίδιο url.
- Για την αναζήτηση με keywords, στέλνουμε post request και πάλι στο url http://ponte.grid.ece.ntua.gr:8280/SMA_Adapter/TwitterServlet με body της μορφής:
{
  "keywords": ["Interventions|Vaccination", "Arthritis", ..., "Pregnancy|Single|Twins"],
  "place": "",
  "logic": "and"
}
- Η παράμετρος keywords όπως βλέπουμε είναι ένας πίνακας από strings διαχωρισμένα με |, τόσα όσες και οι επιλεγμένες λέξεις-κλειδιά. Η πρώτη λέξη κάθε string είναι το επιλεγμένο keyword και οι υπόλοιπες οι υποέννοιές του.
- Το logic παίρνει τιμές and ή or, το place μπορεί να συμπληρωθεί ή να μείνει κενό.

Οδηγίες για τη λειτουργία του service επεξεργασίας δεδομένων:
Ενημερώνουμε τα πεδία domain και port στο αρχείο infos.properties που βρίσκεται στον φάκελο WEB-INF, βάζοντας τα στοιχεία του server στον οποίο έχουμε κάνει deploy το SMA_Adapter.
- Ανεβάζουμε στον server το OntService, και το καλούμε με δύο τρόπους:
1. Για να πάρουμε την οντολογία, στέλνουμε GET request στο url: http://ponte.grid.ece.ntua.gr:8280/OntService/OntServlet. Μπορούμε να στείλουμε αυτό το GET στο service και μέσω του UI, είτε κάνοντας refresh τη σελίδα είτε πατώντας το κουμπί Clear.
2.  Για να ξεκινήσουμε μια αναζήτηση και να πάρουμε τα αποτελέσματα, στέλνουμε POST στο παραπάνω url με json της μορφής:{"user":"test1","password":"1test12!","smedia":"Twitter","items":"Interventions,Lifestyle","mykeywords":"pain,fatigue","logic":"or","akalogic":"yes"}
Μπορούμε φυσικά να στείλουμε POST και μέσω του UI, αν πληκτρολογήσουμε κάποιες λέξεις-κλειδιά στο input field ή επιλέξουμε κάποιες έννοιες από τον κατάλογο και πατήσουμε Submit.

Η αρχική σελίδα της εφαρμογής βρίσκεται στο url http://ponte.grid.ece.ntua.gr:8280/OntService/home.html

Για να λειτουργήσουν σωστά τα παραπάνω πρέπει να έχουμε στον server τον φάκελο lib με όλα τα απαραίτητα αρχεία εξαρτήσεων (.jar), ο οποίος είναι επίσης ανεβασμένος στο repository.
