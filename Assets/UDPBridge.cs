using UnityEngine;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

public class UDPBridge : MonoBehaviour
{
    public int udpPort = 11000; // UDP port to receive messages
    private UdpClient udpClient;
    private Thread receiveThread;
    private bool isRunning = true;

    private PalletCollisionHandler palletCollisionHandler;

    // Thread-safe variables
    private float receivedSpeed = 0f;
    private bool hasNewSpeed = false;

    void Start()
    {
        // Get the PalletCollisionHandler component
        palletCollisionHandler = GetComponent<PalletCollisionHandler>();
        if (palletCollisionHandler == null)
        {
            Debug.LogError("PalletCollisionHandler component not found on this GameObject.");
            return;
        }

        // Initialize UDP client
        udpClient = new UdpClient(udpPort);

        // Start a thread to receive messages
        receiveThread = new Thread(new ThreadStart(ReceiveMessages));
        receiveThread.IsBackground = true;
        receiveThread.Start();
    }

    void Update()
    {
        // Check if a new speed value has been received
        if (hasNewSpeed)
        {
            // Update the speed on the main thread
            palletCollisionHandler.speed = receivedSpeed;
            hasNewSpeed = false; // Reset the flag
        }
    }

    void ReceiveMessages()
    {
        IPEndPoint remoteEndPoint = new IPEndPoint(IPAddress.Any, udpPort);

        while (isRunning)
        {
            try
            {
                // Receive data from the network
                byte[] data = udpClient.Receive(ref remoteEndPoint);
                Debug.Log("Received UDP message from: " + remoteEndPoint.Address + ":" + remoteEndPoint.Port);
                string message = Encoding.UTF8.GetString(data);

                // Parse the received message as a float
                if (float.TryParse(message, out float newSpeed))
                {
                    // Store the received speed value
                    receivedSpeed = newSpeed;
                    hasNewSpeed = true; // Set the flag to indicate a new speed value
                }
                else
                {
                    Debug.LogWarning($"Received invalid speed value: {message}");
                }
            }
            catch (System.Exception e)
            {
                Debug.LogError($"Error receiving UDP message: {e.Message}");
            }
        }
    }

    void OnDestroy()
    {
        // Stop the receive thread and close the UDP client
        isRunning = false;
        udpClient.Close();
    }
}