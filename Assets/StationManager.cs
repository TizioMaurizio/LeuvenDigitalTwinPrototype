using UnityEngine;

public class PalletCollisionHandler : MonoBehaviour
{
    public float speed = 1f;
    public GameObject pallet;
    private Rigidbody palletRigidbody;
    private Vector3 direction;
    private void OnTriggerEnter(Collider collision)
    {
        // Check if the collided object has the tag "Pallet"
        if (collision.gameObject.CompareTag("Pallet"))
        {
            // get children of parent of this object, sent pallet variable of each PalletCollisionHandler in children but this to null
            foreach (Transform child in transform.parent)
            {
                PalletCollisionHandler palletCollisionHandler = child.GetComponent<PalletCollisionHandler>();
                if (child != gameObject)
                {
                    if (palletCollisionHandler.pallet == collision.gameObject)
                    {
                        palletCollisionHandler.pallet = null;
                    }
                }
            }
            pallet = collision.gameObject;
            Debug.Log(pallet.name + " collided with " + gameObject.name);
            // Get the Rigidbody component of the Pallet
            palletRigidbody = pallet.GetComponent<Rigidbody>();
            if (palletRigidbody != null)
            {
                // Find the child GameObject called "Direction"
                Transform directionChild = transform.Find("Direction");
                if (directionChild != null)
                {
                    // Get the up direction of the "Direction" child
                    direction = directionChild.up;

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

    void Update()
    {
        // Rotate the pallet around the Y-axis
        if (pallet != null & palletRigidbody != null)
        {
            palletRigidbody.linearVelocity = direction * speed;
        }
    }
}