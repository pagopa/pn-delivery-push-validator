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
- Persistenza degli stati su DynamoDB
- Comunicazione con servizi esterni dell'ecosistema PN

## Funzionamento del Servizio

1. **Ricezione Eventi**  
   Il servizio riceve messaggi da diverse code SQS specializzate per processare eventi di validazione, azioni programmate e notifiche da servizi esterni.

2. **Elaborazione e Validazione**  
   Ogni messaggio viene processato attraverso handler dedicati che eseguono controlli specifici sui dati della notifica utilizzando service specializzati.

3. **Gestione degli Esiti**
    - **Validazione positiva**: La notifica prosegue nel workflow con aggiornamento dello stato
    - **Validazione fallita**: L'errore viene tracciato e gestito secondo le policy configurate

4. **Persistenza e Comunicazione**  
   Gli stati e i risultati vengono persistiti su DynamoDB e propagati ai servizi downstream tramite code SQS o chiamate REST.

## Panoramica Architetturale
Il microservizio **pn-delivery-push-validator-service** si compone di:
- **Message Handlers**: Processamento asincrono di eventi da code SQS
- **Validation Services**: Logica di business per controlli di validazione
- **Data Persistence**: Gestione stato su tabelle DynamoDB
- **External Integration**: Comunicazione con servizi dell'ecosistema PN

Il servizio implementa pattern event-driven per gestire il flusso di validazione asincrona delle notifiche digitali, processando eventi di tipo `DeliveryPushValidationInput`, `ScheduledValidationAction`, `SafeStorageEvent`, `AddressManagerEvent` e `F24Event`.

### Diagramma Architetturale
 ToDo: inserire il diagramma


---

## DocumentCreationRequestTable (DynamoDB)

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_DOCUMENTCREATIONREQUESTDAO_TABLENAME`
- **Nome risorsa CloudFormation**: `DocumentCreationRequestTableName`
- **Tipo**: Tabella DynamoDB

### Funzionamento
- **Scopo**: Persistenza degli stati e dei dati delle richieste di validazione delle notifiche digitali, inclusi esiti, timestamp, destinatari e metadati.
- **Operazioni**: Lettura e scrittura di item relativi alle notifiche durante il flusso di validazione asincrona.
- **Handler/Service**: Utilizzata dai service e handler di validazione (`DeliveryPushValidationService`, `ScheduledValidationActionHandler`, ecc.).

### Struttura Item
- **Chiave primaria**:
    - `iun` (string) — Identificativo univoco notifica
- **Altri campi principali**:
    - `paProtocolNumber`
    - `recipients`
    - `validationStatus`
    - `lastUpdateTimestamp`
    - `errorDetails` (se presenti errori)

#### Esempio di item
```json
{
  "iun": "ABC123456",
  "paProtocolNumber": "2024-0001",
  "recipients": [
    { "taxId": "XYZ12345A", "status": "VALIDATED" }
  ],
  "validationStatus": "OK",
  "lastUpdateTimestamp": "2024-06-01T12:34:56Z",
  "errorDetails": null
}
```

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

### Struttura Messaggi
- **Tipo di evento**: `DeliveryPushValidationInput`
- **Campi principali**: `iun`, `paProtocolNumber`, `recipients`, `requestTimestamp`
- **Formato**:
  ```json
  {
    "iun": "string",
    "paProtocolNumber": "string",
    "recipients": [ { "taxId": "string" } ],
    "requestTimestamp": "2024-06-01T12:00:00Z"
  }
  ```

### Flusso di Elaborazione
1. Ricezione tramite listener SQS.
2. Validazione integrità e coerenza dati.
3. Processing: aggiornamento stato su DynamoDB e propagazione evento.
4. Output: scrittura su DynamoDB, invio su altre code o chiamate REST.
5. Error Handling: retry, invio a DLQ, logging.

### Interazioni
- **Upstream**: `pn-delivery-push`
- **Downstream**: DynamoDB, ScheduledValidationActionsQueue
- **Dipendenze**: `DeliveryPushValidationService`

### Monitoring & Logging
- **Metriche**: messaggi processati, errori, tempi di validazione.
- **Log**: ricezione, esito, errori, invio a DLQ.
- **Alert**: errori di validazione, soglia DLQ.

---

## ScheduledValidationActionsQueue

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_TOPICS_VALIDATIONACTIONS`
- **Tipo**: Input/Output

### Funzionamento
- **Scopo**: Gestisce azioni pianificate di validazione (retry, escalation).
- **Trigger**: Scheduling automatico o eventi di errore.
- **Handler**: `ScheduledValidationActionHandler.java`

### Struttura Messaggi
- **Tipo di evento**: `ScheduledValidationAction`
- **Campi principali**: `iun`, `actionType`, `scheduledAt`
- **Formato**:
  ```json
  {
    "iun": "string",
    "actionType": "RETRY|ESCALATE",
    "scheduledAt": "2024-06-01T13:00:00Z"
  }
  ```

### Flusso di Elaborazione
1. Ricezione tramite listener SQS.
2. Validazione dati e scheduling.
3. Processing: esecuzione azione pianificata.
4. Output: aggiornamento stato su DynamoDB, invio su altre code.
5. Error Handling: retry, DLQ, logging.

### Interazioni
- **Upstream**: Scheduler interno, orchestratori
- **Downstream**: DynamoDB, altre code
- **Dipendenze**: `ValidationActionService`

### Monitoring & Logging
- **Metriche**: azioni eseguite, errori, retry.
- **Log**: scheduling, esecuzione, errori.
- **Alert**: azioni non eseguite, errori ripetuti.

---

## SafeStorageToDeliveryPushQueue

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_TOPICS_SAFESTORAGEEVENTS`
- **Tipo**: Input

### Funzionamento
- **Scopo**: Riceve eventi da SafeStorage relativi a documenti delle notifiche.
- **Trigger**: Completamento operazioni su documenti in SafeStorage.
- **Handler**: `SafeStorageEventHandler.java`

### Struttura Messaggi
- **Tipo di evento**: `SafeStorageEvent`
- **Campi principali**: `iun`, `documentId`, `eventType`, `timestamp`
- **Formato**:
  ```json
  {
    "iun": "string",
    "documentId": "string",
    "eventType": "UPLOAD_COMPLETED|ERROR",
    "timestamp": "2024-06-01T14:00:00Z"
  }
  ```

### Flusso di Elaborazione
1. Ricezione tramite listener SQS.
2. Validazione tipo evento e integrità dati.
3. Processing: aggiornamento stato notifica/documento.
4. Output: aggiornamento DynamoDB, propagazione evento.
5. Error Handling: logging, retry, DLQ.

### Interazioni
- **Upstream**: SafeStorage
- **Downstream**: DynamoDB, altri servizi
- **Dipendenze**: `SafeStorageEventService`

### Monitoring & Logging
- **Metriche**: eventi ricevuti, errori, tempi di processing.
- **Log**: ricezione evento, esito, errori.
- **Alert**: errori upload, eventi non processati.

---

## AddressManagerToDeliveryPushQueue

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_TOPICS_ADDRESSMANAGEREVENTS`
- **Tipo**: Input

### Funzionamento
- **Scopo**: Riceve aggiornamenti sugli indirizzi dei destinatari delle notifiche.
- **Trigger**: Modifica/aggiornamento indirizzo da AddressManager.
- **Handler**: `AddressManagerEventHandler.java`

### Struttura Messaggi
- **Tipo di evento**: `AddressManagerEvent`
- **Campi principali**: `iun`, `recipientIndex`, `address`, `eventType`
- **Formato**:
  ```json
  {
    "iun": "string",
    "recipientIndex": 0,
    "address": "string",
    "eventType": "ADDRESS_UPDATED"
  }
  ```

### Flusso di Elaborazione
1. Ricezione tramite listener SQS.
2. Validazione coerenza dati indirizzo.
3. Processing: aggiornamento dati destinatario.
4. Output: aggiornamento DynamoDB, propagazione.
5. Error Handling: logging, retry, DLQ.

### Interazioni
- **Upstream**: AddressManager
- **Downstream**: DynamoDB, servizi di notifica
- **Dipendenze**: `AddressManagerEventService`

### Monitoring & Logging
- **Metriche**: eventi indirizzo ricevuti, errori.
- **Log**: ricezione, aggiornamento, errori.
- **Alert**: errori aggiornamento indirizzo.

---

## F24ToDeliveryPushQueue

### Configurazione
- **Variabile d'ambiente**: `PN_DELIVERYPUSHVALIDATOR_TOPICS_F24EVENTS`
- **Tipo**: Input

### Funzionamento
- **Scopo**: Riceve eventi relativi a pagamenti F24 associati alle notifiche.
- **Trigger**: Evento di pagamento F24 da sistema esterno.
- **Handler**: `F24EventHandler.java`

### Struttura Messaggi
- **Tipo di evento**: `F24Event`
- **Campi principali**: `iun`, `paymentId`, `status`, `timestamp`
- **Formato**:
  ```json
  {
    "iun": "string",
    "paymentId": "string",
    "status": "PAID|FAILED",
    "timestamp": "2024-06-01T15:00:00Z"
  }
  ```

### Flusso di Elaborazione
1. Ricezione tramite listener SQS.
2. Validazione stato pagamento e dati.
3. Processing: aggiornamento stato notifica/pagamento.
4. Output: aggiornamento DynamoDB, propagazione.
5. Error Handling: logging, retry, DLQ.

### Interazioni
- **Upstream**: Sistema F24 esterno
- **Downstream**: DynamoDB, servizi di notifica
- **Dipendenze**: `F24EventService`

### Monitoring & Logging
- **Metriche**: eventi F24 ricevuti, errori.
- **Log**: ricezione, esito pagamento, errori.
- **Alert**: pagamenti falliti, errori di processing.

---
## Componenti

### pn-delivery-push-validator-service

#### Responsabilità
- Legge e scrive sulle code SQS: DeliveryPushValidationInputsQueue, ScheduledValidationActionsQueue, SafeStorageToDeliveryPushQueue, AddressManagerToDeliveryPushQueue, F24ToDeliveryPushQueue
- Legge e scrive sulla tabella DynamoDB: DocumentCreationRequestTable
- Espone endpoint REST per la validazione delle richieste di consegna (`/delivery/validate`) e per il recupero dello stato (`/delivery/status/{id}`)
- Implementa logica di validazione tramite i service (`DeliveryPushValidationService`, `ValidationActionService`) e handler (`DeliveryPushValidationHandler`, `ScheduledValidationActionHandler`)
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
4. Maven 3.8+ installato per la build e l'esecuzione dei test
5. Variabili d'ambiente configurate come da tabella sopra (puoi usare un file `.env` o esportarle nel terminale)
6. Code SQS e tabella DynamoDB create su Localstack (puoi usare i template CloudFormation in `scripts/aws/cfn/` oppure gli script Node.js in `js/`)
7. (Opzionale) File di configurazione custom in `src/main/resources/application-local.yml` per override locale
