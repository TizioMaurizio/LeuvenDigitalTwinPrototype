import socket

# Configuration
UDP_IP = "127.0.0.1"  # Localhost
UDP_PORT = 11002      # Port must match the Unity script

# Function to send speed value
def send_speed(speed):
    try:
        # Create a UDP socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        
        # Convert the speed to a string and encode it
        message = str(speed).encode('utf-8')
        
        # Send the message to the Unity application
        sock.sendto(message, (UDP_IP, UDP_PORT))
        print(f"Sent speed: {speed}")
        
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
            
            # Send the speed value to Unity
            send_speed(speed)
        except ValueError:
            print("Please enter a valid number.")
        except KeyboardInterrupt:
            print("Exiting...")
            break