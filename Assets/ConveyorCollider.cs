using UnityEngine;
using System.Collections.Generic;

public class ConveyorCollider : MonoBehaviour
{
    //list of gameobjects collidingPallets
    public List<GameObject> collidingPallets = new List<GameObject>();
    //ontriggerstay check if colliding object has tag Pallet, if yes add to collidingPallets
    void OnTriggerEnter(Collider other)
    {
        if (other.tag == "Pallet")
        {
            collidingPallets.Add(other.gameObject);
        }
    }
    void OnTriggerExit(Collider other)
    {
        if (other.tag == "Pallet")
        {
            collidingPallets.Remove(other.gameObject);
        }
    }
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
