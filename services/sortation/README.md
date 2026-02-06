
### Co Spring Boot zrobi automatycznie mając natywne nazwy properties dla kafki w appliaction.properties oraz zależność spring-boot-starter-kafka:

1. Zbuduje bean KafkaStreamsConfiguration na podstawie spring.kafka.* i spring.kafka.streams.*.
To jest wewnętrznie mapa Map<String, Object> z ustawieniami Kafka Streams.
2. Utworzy i uruchomi KafkaStreams jeśli dostarczysz topologię (np. jako bean StreamsBuilderFactoryBean / KStream/KTable zbudowane na StreamsBuilder).
3. Podpina lifecycle do Springa 
* startuje streams przy starcie aplikacji,
* zamyka przy shutdown,
* integruje się z Actuatorem (w zależności od wersji/starterów).


### A jak to połączyć z własnym kafka config?
Jeśli zdefiniujesz własny bean o nazwie defaultKafkaStreamsConfig (czyli DEFAULT_STREAMS_CONFIG_BEAN_NAME), 
<br>
to auto-konfiguracja Spring Boot powinna uznać, że już masz konfigurację i nie tworzyć drugiej.
(jeśli zrobisz to „podwójnie”, dostaniesz konflikt nazw beana na starcie (klasyczny błąd „bean already defined, overriding disabled”). 
<br>
Taki przypadek jest znany i wynika właśnie z tego, że Boot próbuje zarejestrować swój bean defaultKafkaStreamsConfig,
a ty rejestrujesz drugi o tej samej nazwie)


### Jak można połączyć application.properties z twoim „manualnym beaniem”?
Jeśli zdecydujesz się na manualną, to nie musisz wracać do @Value("${kafka.application-id}") itd.
<br>
Zamiast tego:

1) bierzesz bazowe propsy z Boota (KafkaProperties.buildStreamsProperties()),
2) nadpisujesz / dopisujesz tylko to, co chcesz mieć pod kontrolą (np. processing.guarantee, cache, commit interval),
3) dodajesz ustawienia pod Apicurio Registry (te z tracking: headers/globalId/SpecificReader) — to też jest element “dlaczego dodajemy”.

### Dlaczego starter dla kafki był potrzebny?
W Spring Boot 4 Spring poszedł w mocną modularność: auto-konfiguracje nie siedzą już w jednym wielkim “worku”,
<br>
tylko w osobnych modułach. Dla Kafki powstał moduł spring-boot-kafka, a starter spring-boot-starter-kafka go dociąga.

### Błąd topologi
![img.png](img.png)

### Po co topic config?
![img_1.png](img_1.png)