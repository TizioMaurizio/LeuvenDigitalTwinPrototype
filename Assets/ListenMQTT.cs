using UnityEngine;
using uPLibrary.Networking.M2Mqtt;
using uPLibrary.Networking.M2Mqtt.Messages;
using System;

public class MqttListener : MonoBehaviour
{
    private MqttClient client;

    // MQTT broker settings
    private string brokerAddress = "127.0.0.1";
    private int brokerPort = 1883; // Default MQTT port
    private string clientId = "UnityClient";

    // Topic to subscribe to (wildcard #)
    private string topic = "#";

    void Start()
    {
        try
        {
            // Create a new MQTT client instance
            client = new MqttClient(brokerAddress, brokerPort, false, null, null, MqttSslProtocols.None);

            // Register to message received event
            client.MqttMsgPublishReceived += OnMessageReceived;

            // Connect to the broker
            client.Connect(clientId);

            if (client.IsConnected)
            {
                Debug.Log("Connected to MQTT broker");

                // Subscribe to the topic with wildcard #
                client.Subscribe(new string[] { topic }, new byte[] { MqttMsgBase.QOS_LEVEL_AT_LEAST_ONCE });
                Debug.Log("Subscribed to topic: " + topic);
            }
            else
            {
                Debug.LogError("Failed to connect to MQTT broker");
            }
        }
        catch (Exception ex)
        {
            Debug.LogError("MQTT Error: " + ex.Message);
        }
    }

    private void OnMessageReceived(object sender, MqttMsgPublishEventArgs e)
    {
        // Convert the received message to a string
        string message = System.Text.Encoding.UTF8.GetString(e.Message);

        // Log the received message
        Debug.Log("Received message on topic: " + e.Topic + " Message: " + message);
    }

    void OnDestroy()
    {
        if (client != null && client.IsConnected)
        {
            // Unsubscribe and disconnect when the object is destroyed
            client.Unsubscribe(new string[] { topic });
            client.Disconnect();
            Debug.Log("Disconnected from MQTT broker");
        }
    }
}