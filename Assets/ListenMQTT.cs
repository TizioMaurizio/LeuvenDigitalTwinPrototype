using System.Collections.Generic;
using UnityEngine;
using M2MqttUnity;
using uPLibrary.Networking.M2Mqtt.Messages;

public class MqttReceiver : M2MqttUnityClient
{
    [Header("MQTT Settings")]
    public List<string> topics = new List<string>(); // List of topics to subscribe to

    // Event triggered when a new message is received
    public delegate void MessageReceived(string topic, string message);
    public event MessageReceived OnMessageReceived;

    protected override void SubscribeTopics()
    {
        if (topics.Count > 0)
        {
            client.Subscribe(topics.ToArray(), new byte[topics.Count]);
            Debug.Log("Subscribed to topics: " + string.Join(", ", topics));
        }
    }

    protected override void UnsubscribeTopics()
    {
        if (topics.Count > 0)
        {
            client.Unsubscribe(topics.ToArray());
            Debug.Log("Unsubscribed from topics: " + string.Join(", ", topics));
        }
    }

    protected override void DecodeMessage(string topic, byte[] message)
    {
        string msg = System.Text.Encoding.UTF8.GetString(message);
        Debug.Log($"Received message on topic '{topic}': {msg}");
        OnMessageReceived?.Invoke(topic, msg);
    }

    public void PublishMessage(string topic, string message)
    {
        if (client != null && client.IsConnected)
        {
            client.Publish(topic, System.Text.Encoding.UTF8.GetBytes(message), MqttMsgBase.QOS_LEVEL_AT_LEAST_ONCE, false);
            Debug.Log($"Published message to topic '{topic}': {message}");
        }
    }
}
