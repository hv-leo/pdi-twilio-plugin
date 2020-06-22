# SMS Sender
The SMS Sender step allows you to send SMS messages using Twilio Java API.

### Example
- In this example, I am sending 3 well-formed SMS messages, and another 2 with invalid phone numbers.

![alt text](https://github.com/LeonardoCoelho71950/pdi-twilio-plugin/blob/master/docs/screenshots/example.png "Send SMS messages using Twilio API.")

- After executing the transformation, we can see which messages were successfully sent:

![alt text](https://github.com/LeonardoCoelho71950/pdi-twilio-plugin/blob/master/docs/screenshots/sentMessages.png "SMS messages that were successfully sent.")

- And of course, the receiver will receive the SMS messages:

![alt text](https://github.com/LeonardoCoelho71950/pdi-twilio-plugin/blob/master/docs/screenshots/messages.png "SMS messages.")

- We can also check for the failed messages:

![alt text](https://github.com/LeonardoCoelho71950/pdi-twilio-plugin/blob/master/docs/screenshots/failedMessages.png "SMS messages that failed.")

- The transformation logs provides us some tips on why these messages failed:

![alt text](https://github.com/LeonardoCoelho71950/pdi-twilio-plugin/blob/master/docs/screenshots/logs.png "Transformation logs.")
