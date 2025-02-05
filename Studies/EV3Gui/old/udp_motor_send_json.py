import socket

# Configuration
UDP_IP = "127.0.0.1"  # Localhost
UDP_PORT = 11003       # Port must match the Unity script

# Function to send speed value
def send_speed(json_message):
    try:
        # Create a UDP socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        
        # Convert the speed to a string and encode it
        message = str(json_message).encode('utf-8')
        
        # Send the message to the Unity application
        sock.sendto(message, (UDP_IP, UDP_PORT))
        print(f"Sent message: {message}")
        
        # Close the socket
        sock.close()
    except Exception as e:
        print(f"Error sending UDP message: {e}")

# Example usage
if __name__ == "__main__":
    while True:
        try:
            # Get the speed value from user input
            speed = input("Enter speed value: ")
            
            speed = str(speed).replace(".", ",")
            
            json_message = {
                "outA": speed,
                "outB": speed,
                "outC": speed,
            }
            
            # Send the speed value to Unity
            send_speed(json_message)
        except ValueError:
            print("Please enter a valid number.")
        except KeyboardInterrupt:
            print("Exiting...")
            break