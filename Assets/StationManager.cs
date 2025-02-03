using UnityEngine;

public class PalletCollisionHandler : MonoBehaviour
{
    public float speed = 1f;
    private void OnTriggerStay(Collider collision)
    {
        // Check if the collided object has the tag "Pallet"
        if (collision.gameObject.CompareTag("Pallet"))
        {
            Debug.Log("Pallet collided with " + collision.gameObject.name);
            // Get the Rigidbody component of the Pallet
            Rigidbody palletRigidbody = collision.gameObject.GetComponent<Rigidbody>();
            if (palletRigidbody != null)
            {
                // Find the child GameObject called "Direction"
                Transform directionChild = transform.Find("Direction");
                if (directionChild != null)
                {
                    // Get the up direction of the "Direction" child
                    Vector3 direction = directionChild.up;

                    // Normalize the direction and set the velocity
                    direction.Normalize();
                    palletRigidbody.linearVelocity = direction * speed;
                }
                else
                {
                    Debug.LogWarning("Child GameObject named 'Direction' not found!");
                }
            }
            else
            {
                Debug.LogWarning("Collided object with tag 'Pallet' does not have a Rigidbody component!");
            }
        }
    }
}