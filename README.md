1 currency-provider
- aplikacja ma za zadanie cyklicznie (co 5 minut) pobierać informacje
  na temat kursów walut z zewnętrznego api (przykładowo NBP).
- po pobraniu informacji o kursach kupna i sprzedaży każdej waluty
  przekazuje na kolejkę (przykładowo RabbitMQ) wiadomości z danymi
  dla każdej waluty oddzielnie.

2 currency-persistence-service
- aplikacja ma zbierać z kolejki wiadomości z informacjami na temat
  kursów walut i zapisywać je do bazy danych.
- baza danych ma przechowywać tylko najbardziej aktualną informację
  o kursie kupna i sprzedaży dla danej waluty.

3 exchange-api
- aplikacja ma wystawiać endpointy, które pozwalają na uzyskanie
  informacji takich jak
    - wszystkie dostępne waluty (brak autentykacji)
    - informacje o kursach wskazanej waluty (brak autentykacji)
    - wymiana walut z potwierdzeniem (użytkownik TRADER)
- aplikacja ma używać security i weryfikować użytkowników zgodnie
  z powyższą rozspiską usług.
- wysyłka danych potrzebnych do potwierdzenia ma być wykonana
  przez rabbita do 4 aplikacji i ma zawierać adres mailowy
  przypisany do użytkownika wysyłającego zapytanie.

4 mail-service
- ma przyjmować informację o wymianie walut i adresie użytkownika,
  oraz budować i wysyłać wiadomość mailową na wskazany adres.

docelowo wszystkie komponenety razem z kolejkami i bazą mają być
możliwe do uruchomienia przez wywołanie z docker-compose


testy jednostkowe + integracyjne
integracyjne będą wymagały użycia Wiremock oraz Testcontainers


security oparte o keycloak (najlepiej zrobić jak już wszystko będzie gotowe, bo będzie przeszkadzało w developmencie)