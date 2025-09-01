# pn-delivery-push-validator-service

**pn-delivery-push-validator-service** è un microservizio dedicato alla gestione della fase di validazione asincrona delle notifiche nell'ecosistema PagoPA Notifiche Digitali,
con requisiti stringenti di SLA (24 ore per la validazione). Il servizio riceve messaggi dalla coda SQS `pn-delivery-push-validation-input` contenenti richieste di notificazione che hanno già superato una validazione sincrona iniziale.
Esegue controlli di coerenza, integrità e completezza sui dati delle notifiche tramite service e handler dedicati. In base all’esito della validazione, la notifica viene accettata (e il flusso prosegue verso i microservizi successivi) oppure rifiutata (con tracciamento dell’errore).
Gli esiti e i dati delle notifiche vengono salvati su DynamoDB e, se necessario, propagati verso altre code SQS o microservizi tramite chiamate REST.

## Panoramica del Servizio
Il servizio gestisce la validazione asincrona delle notifiche digitali attraverso:
- Elaborazione di messaggi da code SQS dedicate
- Controlli di coerenza, integrità e completezza dei dati
- Orchestrazione delle azioni di validazione
- Comunicazione con servizi esterni dell'ecosistema PN

## Funzionamento del Servizio

1. **Ricezione Eventi**  
   Il servizio riceve messaggi da diverse code SQS specializzate per processare eventi di validazione, azioni programmate e notifiche da servizi esterni.

2. **Elaborazione e Validazione**  
   Ogni messaggio viene processato attraverso handler dedicati che eseguono controlli specifici sui dati della notifica utilizzando service specializzati.

3. **Gestione degli Esiti**
    - **Validazione positiva**: La notifica prosegue nel workflow con aggiornamento dello stato
    - **Validazione fallita**: L'errore viene tracciato mediante un apposito elemento di timeline in cui sono riportati i dettagli dell'errore.

4. **Persistenza e Comunicazione**  
   Gli stati e i risultati vengono persistiti su DynamoDB e propagati ai servizi downstream tramite code SQS.

## Panoramica Architetturale
Il microservizio **pn-delivery-push-validator-service** si compone di:
- **Message Handlers**: Processamento asincrono di eventi da code SQS
- **Validation Services**: Logica di business per controlli di validazione
- **External Integration**: Comunicazione con servizi dell'ecosistema PN

Il servizio implementa pattern event-driven per gestire il flusso di validazione asincrona delle notifiche digitali, processando eventi ricevuti dalle code `DeliveryPushValidationInput`, `ScheduledValidationAction`, `SafeStorageEvent`, `AddressManagerEvent` e `F24Event`.

### Diagramma Architetturale
 ToDo: inserire il diagramma


---

## DocumentCreationRequestTable (DynamoDB)

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_DOCUMENTCREATIONREQUESTDAO_TABLENAME`
- **Nome risorsa CloudFormation**: `DocumentCreationRequestTableName`
- **Tipo**: Tabella DynamoDB


## Code SQS gestite dal servizio

---

## DeliveryPushValidationInputsQueue

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_TOPICS_DELIVERYVALIDATIONEVENTS`
- **Tipo**: Input

### Funzionamento
- **Scopo**: Riceve richieste di validazione asincrona delle notifiche digitali.
- **Trigger**: Invio di una richiesta di notifica da parte di un ente, dopo la validazione sincrona.
- **Handler**: `DeliveryPushValidationHandler.java`
---

## ScheduledValidationActionsQueue

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_TOPICS_VALIDATIONACTIONS`
- **Tipo**: Input/Output

### Funzionamento
- **Scopo**: Gestisce azioni pianificate di validazione (retry, escalation).
- **Trigger**: Scheduling automatico o eventi di errore.
- **Handler**: `ScheduledValidationActionHandler.java`
---

## SafeStorageToDeliveryPushQueue

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_TOPICS_SAFESTORAGEEVENTS`
- **Tipo**: Input

### Funzionamento
- **Scopo**: Riceve eventi da SafeStorage relativi a documenti delle notifiche.
- **Trigger**: Completamento operazioni su documenti in SafeStorage.
- **Handler**: `SafeStorageEventHandler.java`
---

## AddressManagerToDeliveryPushQueue

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_TOPICS_ADDRESSMANAGEREVENTS`
- **Tipo**: Input

### Funzionamento
- **Scopo**: Riceve aggiornamenti sugli indirizzi dei destinatari delle notifiche.
- **Trigger**: Modifica/aggiornamento indirizzo da AddressManager.
- **Handler**: `AddressManagerEventHandler.java`
---

## F24ToDeliveryPushQueue

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_TOPICS_F24EVENTS`
- **Tipo**: Input

### Funzionamento
- **Scopo**: Riceve eventi relativi a pagamenti F24 associati alle notifiche.
- **Trigger**: Evento di pagamento F24 da sistema esterno.
- **Handler**: `F24EventHandler.java`
---
## Componenti

### pn-delivery-push-validator-service

#### Responsabilità
- Legge dalle code SQS: DeliveryPushValidationInputsQueue, ScheduledValidationActionsQueue, SafeStorageToDeliveryPushQueue, AddressManagerToDeliveryPushQueue, F24ToDeliveryPushQueue
- Legge e scrive sulla tabella DynamoDB: DocumentCreationRequestTable
- Processa messaggi/eventi di tipo:
    - DeliveryPushValidationInput (richieste di validazione)
    - ScheduledValidationAction (azioni pianificate)
    - SafeStorageEvent (eventi da SafeStorage)
    - AddressManagerEvent (eventi da AddressManager)
    - F24Event (eventi F24)
- Gestisce errori e logging tramite SLF4J/Logback

#### Configurazione
| Variabile Ambiente                                            | Descrizione                  | Default | Obbligatorio |
|---------------------------------------------------------------|------------------------------|---------|--------------|
| AWS_REGIONCODE                                                | N/A                          | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_TOPICS_VALIDATIONACTIONS             | Coda azioni di validazione   | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_TOPICS_SAFESTORAGEEVENTS             | Coda eventi SafeStorage      | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_TOPICS_ADDRESSMANAGEREVENTS          | Coda eventi AddressManager   | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_TOPICS_F24EVENTS                     | Coda eventi F24              | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_TOPICS_DELIVERYVALIDATIONEVENTS      | Coda input validazione       | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_DOCUMENTCREATIONREQUESTDAO_TABLENAME | Tabella DynamoDB richieste   | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_F24CXID                              | ClientId per pn-f24          | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_F24BASEURL                           | Base URL pn-f24              | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_SAFESTORAGEBASEURL                   | Base URL SafeStorage         | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_DELIVERYBASEURL                      | Base URL delivery            | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_EXTERNALREGISTRYBASEURL              | Base URL registri esterni    | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_NATIONALREGISTRIESBASEURL            | Base URL registri nazionali  | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_TEMPLATESENGINEBASEURL               | Base URL template engine     | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_TIMELINECLIENTBASEURL                | Base URL timeline client     | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_ACTIONMANAGERBASEURL                 | Base URL action manager      | -       | Si           |
| PN_DELIVERYPUSHVALIDATOR_DELIVERYPUSHBASEURL                  | Base URL delivery push       | -       | Si           |
| PN_CRON_ANALYZER                                              | Cron per metriche CloudWatch | -       | No           |
| WIRE_TAP_LOG                                                  | Attivazione wire logs        | -       | No           |

## Testing in locale

### Prerequisiti
1. Docker/Podman avviato con container di Localstack (puoi utilizzare il Docker Compose di [Localdev](https://github.com/pagopa/pn-localdev))
2. Java 17 installato e configurato nel `PATH`
3. Node.js (>=18) e npm installati per l'esecuzione degli script di supporto