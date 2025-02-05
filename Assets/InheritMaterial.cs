using UnityEngine;

public class InheritMaterial : MonoBehaviour
{
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        //set material of this gameobject to material of father
        Renderer renderer = GetComponent<Renderer>();
        if (renderer != null)
        {
            renderer.material = transform.parent.parent.GetComponent<Renderer>().material;
        }
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
