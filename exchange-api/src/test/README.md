# Testy Exchange API

## Struktura testów

### 1. Testy integracyjne (`CurrencyExchangeControllerIntegrationTest`)

Testy integracyjne wykorzystują **Testcontainers** do uruchomienia prawdziwych instancji MongoDB i RabbitMQ w kontenerach Docker.

**Testowane endpointy:**
- `GET /api/v1/currencies` - pobieranie wszystkich walut
- `GET /api/v1/currencies/{code}` - pobieranie konkretnej waluty
- `POST /api/v1/currencies/exchange` - wymiana walut

**Scenariusze testowe:**
1. ✅ Zwraca wszystkie waluty
2. ✅ Zwraca pustą listę gdy brak walut
3. ✅ Zwraca konkretną walutę po kodzie
4. ✅ Zwraca 404 gdy waluta nie istnieje
5. ✅ Wykonuje wymianę walut USD→EUR
6. ✅ Wykonuje wymianę USD→PLN (używa buyRate)
7. ✅ Wykonuje wymianę PLN→EUR (używa sellRate)
8. ✅ Zwraca 400 przy wymianie tej samej waluty

### 2. Testy jednostkowe (`CurrencyServiceTest`)

Testy jednostkowe serwisu z użyciem **Mockito** do mockowania zależności.

**Testowane metody:**
- `getAllCurrencyRates()` - pobieranie wszystkich walut
- `getCurrencyByCode(String code)` - pobieranie waluty po kodzie
- `exchangeCurrency(ExchangeRequestCommand, Jwt)` - wymiana walut

**Scenariusze testowe:**
1. ✅ Zwraca zmapowane DTOs
2. ✅ Zwraca pustą listę gdy brak walut
3. ✅ Zwraca walutę gdy znaleziona
4. ✅ Rzuca wyjątek gdy waluta nie znaleziona
5. ✅ Wymiana USD→PLN (używa buyRate: 100 * 4.10 = 410)
6. ✅ Wymiana PLN→EUR (używa sellRate: 100 / 4.60 = 21.74)
7. ✅ Wymiana USD→EUR (dwuetapowa przez PLN: 100 * 4.10 / 4.60 = 89.13)
8. ✅ Rzuca wyjątek przy tej samej walucie
9. ✅ Weryfikuje wysłanie wiadomości do RabbitMQ
10. ✅ Weryfikuje dodanie emaila z JWT do wyniku

### 3. Testy mappera (`CurrencyRateMapperTest`)

Testy mapowania encji na DTO.

**Scenariusze testowe:**
1. ✅ Poprawnie mapuje wszystkie pola
2. ✅ Obsługuje null w ratach

## Uruchomienie testów

### Wymagania:
- Docker (dla Testcontainers)
- Java 17
- Maven

### Uruchomienie wszystkich testów:
```bash
mvn test
```

### Uruchomienie tylko testów integracyjnych:
```bash
mvn test -Dtest=*IntegrationTest
```

### Uruchomienie tylko testów jednostkowych:
```bash
mvn test -Dtest=*Test -Dtest=!*IntegrationTest
```

## Konfiguracja testowa

### BaseIntegrationTest
Klasa bazowa dla testów integracyjnych, która:
- Uruchamia MongoDB w kontenerze (port losowy)
- Uruchamia RabbitMQ w kontenerze (port losowy)
- Konfiguruje właściwości Spring dynamicznie
- Wyłącza OAuth2 security dla testów

### TestSecurityConfig
Konfiguracja Spring Security dla testów - wyłącza wszystkie zabezpieczenia.

### CurrencyExchangeTestHelper
Klasa pomocnicza z metodami do:
- Tworzenia przykładowych danych testowych (USD, EUR, GBP)
- Serializacji obiektów do JSON
- Tworzenia mock JWT tokenów

## Przykładowe dane testowe

### USD Rate:
- Code: "USD"
- Buy Rate: 4.10 PLN
- Sell Rate: 4.20 PLN

### EUR Rate:
- Code: "EUR"
- Buy Rate: 4.50 PLN
- Sell Rate: 4.60 PLN

### GBP Rate:
- Code: "GBP"
- Buy Rate: 5.20 PLN
- Sell Rate: 5.30 PLN

## Logika wymiany walut

1. **X → PLN**: `amount * buyRate`
2. **PLN → X**: `amount / sellRate`
3. **X → Y**: `(amount * X.buyRate) / Y.sellRate`

## Coverage

Testy pokrywają:
- ✅ Controller layer (wszystkie endpointy)
- ✅ Service layer (cała logika biznesowa)
- ✅ Mapper layer (transformacje danych)
- ✅ Integracja z MongoDB (CRUD operacje)
- ✅ Integracja z RabbitMQ (wysyłanie wiadomości)
- ✅ Obsługa błędów (404, 400)

## Wzorce testowe (zgodnie z instrukcją)

Testy zostały napisane zgodnie ze wzorcami z `LessonControllerIntegrationTest`:

1. ✅ Użycie Testcontainers dla prawdziwych zależności
2. ✅ Cleanup przed każdym testem (@BeforeEach)
3. ✅ Struktura Given-When-Then
4. ✅ Weryfikacja odpowiedzi HTTP (status, content-type, JSON)
5. ✅ Weryfikacja persystencji danych
6. ✅ Testy unit z Mockito i ArgumentCaptor
7. ✅ Weryfikacja interakcji z mockami
8. ✅ Testy negatywne (błędy, wyjątki)
