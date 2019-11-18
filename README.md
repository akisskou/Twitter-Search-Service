# Twitter-Search-Service
A project for smart twitter search 

Οδηγίες για τη λειτουργία του service αναζήτησης στο Twitter:
- Μετά το ανέβασμα του SMA_Adapter στον server, η αρχικοποίηση και εκκίνηση του service γίνεται με τον ίδιο ακριβώς τρόπο όπως και πριν. Πρώτα στέλνουμε post στο url http://ponte.grid.ece.ntua.gr:8080/SMA_Adapter/configure με τα credentials στο body, έπειτα get στο http://ponte.grid.ece.ntua.gr:8080/SMA_Adapter/begin
- Για την αναζήτηση με keywords, στέλνουμε post request στο url http://ponte.grid.ece.ntua.gr:8080/SMA_Adapter/retrieve με body της μορφής:
{
  "keywords": ["Interventions|Vaccination", "Arthritis", ..., "Pregnancy|Single|Twins"],
  "place": "",
  "logic": "or",
  "akalogic": "no"
}
