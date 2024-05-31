# Tema proiect

Costurile dinamice ale sistemelor Cloud, cum ar fi Amazon’s Spot Instances, au preturi care variaza in functie de cerere.

Acest lucru ar putea provoca o volatilitate ridicata a preturilor, ceea ce duce la intreruperea executiei mai des decat se poate astepta.

Scopul acestui proiect este de a dezvolta un planificator care poate satisface anumite capacitati de resurse (de exemplu, memorie si procesor) pentru joburile noastre cu o probabilitate specifica, astfel incat acestea sa fie neintrerupte din executie pentru urmatoarea perioada de timp (de exemplu, o zi).

Fiecare sistem cloud este alcatuit din mai multe piete definite in functie de locatie si tipul de resursa. Fiecare job poate fi mutat de la o locatie la alta prin pornirea si oprirea sa.

O aplicatie tipica ce ar beneficia de aceasta poate fi una map-reduce sensibila la timp, in cazul in care, daca un nod este oprit, se aplica o penalizare pentru restabilirea starii sale de pe disc (atat ca timp cat si ca latime de banda).

Prin mutarea dinamica a instantelor catre piete mai sigure (care ar putea costa mai mult), costul job-urilor Big Data poate fi redus, prin urmare se realizeaza un compromis intre cost si disponibilitate.
Se pot efectua executari suplimentare pentru joburi care sunt legate de o anumita locatie prin ajustarea dinamica a timpului instantaneu la volatilitatea pietei pentru a reduce pierderile de executie sau prin duplicarea locurilor de executie pe diferite piete pentru a preveni pierderea disponibilitatii.
