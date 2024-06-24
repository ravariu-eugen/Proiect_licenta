# Arhitectura

Optiuni:

- Rulare pe calculator propriu cu aplicatie
- Rulare in cloud + portal web

Informatii necesare:

- costul pt spot
- starea tuturor instantelor dintr-un task
  - ce fac daca o intanta a unui task a esuat?
    - reset
    - eroare + oprire
    - verific tipul de eroare
- starea tutoror task-urilor dintr-un job
  - ce fac cand un task esueaza





Arhitectura: 

un proces manager central care creeaza instantele

fiecare instanta are un proces worker care controleaza containerele