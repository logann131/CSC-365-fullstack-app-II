package yelp.dataset.oswego.yelpbackend.dataStructure.btree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import yelp.dataset.oswego.yelpbackend.models.BusinessModel;

@Data
@NoArgsConstructor
public class BusinessBNode implements Serializable{
    protected List<BusinessModel> BKeys;  // Array of business key
    protected List<BusinessBNode> BChild;  // Array of business children
    protected int BMinDeg; // Minimum degree (defines the range for number of keys)
    protected int BKeyNum; // number of business keys
    protected boolean BIsLeaf; 

    public BusinessBNode(int BMinDeg, boolean BIsLeaf) {
        this.BKeys = new ArrayList<>();
        this.BChild = new ArrayList<>();
        this.BMinDeg = BMinDeg;
        this.BIsLeaf = BIsLeaf;
        this.BKeyNum = 0;
        for (int i=0; i < (2*BMinDeg-1); i++) {
            this.BKeys.add(new BusinessModel());
        }
        for (int i=0; i < (2*BMinDeg); i++) {
            this.BChild.add(new BusinessBNode());
        }
        
    }


    // A function to traverse all nodes in a subtree rooted with this node
    protected void traverse() {
        
        // There are n keys and n+1 children, traverse through n keys and first n children
        int i = 0;
        for (i = 0; i < this.BKeyNum; i++) {
            
            // If this is not leaf, then before printing BKey[i], traverse the subtree rooted with child BChild[i].
            if (!this.BIsLeaf) BChild.get(i).traverse();
            
            System.out.print(BKeys.get(i) + " ");
        }
 
        // Print the subtree rooted with last child
        if (!BIsLeaf) BChild.get(i).traverse();

    }

    // A function to search a key in the subtree rooted with this node.
    protected BusinessBNode findNode(BusinessModel key) { // returns NULL if k is not present.
        // Find the first key greater than or equal to k
        int i = 0;
        while (i < this.BKeyNum && key.getId() > BKeys.get(i).getId())
            i++;
 
        // If the found key is equal to k, return this node
        if (BKeys.get(i).getId() == key.getId())
            return this;
 
        // If the key is not found here and this is a leaf node => null
        if (BIsLeaf) return null;
 
        // Go to the appropriate child
        return BChild.get(i).findNode(key);
 
    }

    // A function to search a key in the subtree rooted with this node.
    public BusinessModel searchKey(BusinessModel key) { // returns NULL if k is not present.
 
        // Find the first key greater than or equal to k
        int i = 0;
        while (i < this.BKeyNum && key.getId() > BKeys.get(i).getId())
            i++;
 
        // If the found key is equal to k, return this node
        if (BKeys.get(i).getId() == key.getId())
            return this.BKeys.get(i);
 
        // If the key is not found here and this is a leaf node => null
        if (BIsLeaf) return null;
 
        // Go to the appropriate child
        return BChild.get(i).searchKey(key);
 
    }

    /*  
    *   ref: https://www.geeksforgeeks.org/insert-operation-in-b-tree/
    *   add a new key to a non-full node 
    */
    protected void addKey(BusinessModel key) {

        // Init tail
        int tail = BKeyNum - 1;

        // if this is a leaf node
        if (BIsLeaf) {
            // find the appropriate location of the new key 
            while (tail >= 0 && BKeys.get(tail).getId() > key.getId()) {
                BKeys.set(tail+1, BKeys.get(tail)); // move all greater keys to one place ahead to make room for new key
                tail--;
            }

            // Add the new key
            BKeys.set(tail+1, key);
            BKeyNum += 1;
        } else { // if this node is not a leaf
            
            // first find the appropriate child for the new key
            while (tail >= 0 && BKeys.get(tail).getId() > key.getId()) tail--;
            
            if (BChild.get(tail+1).BKeyNum == (2 * BMinDeg -1)) { // if the child is full

                // split the child if full
                splitChild(tail+1, BChild.get(tail+1));

                /* 
                * After split, the middle key of BChild.get(tail) goes up to the node above
                * Bchild.get(tail) is splitted into two children
                * find the appropriate child to add the new key
                */
                if (BKeys.get(tail+1).getId() < key.getId()) tail++;
            }
            BChild.get(tail+1).addKey(key);
        }

    }

    /* 
    * ref: https://www.geeksforgeeks.org/insert-operation-in-b-tree/
    * split the child newNode => newNode must be full to split
    */
    protected void splitChild(int pos, BusinessBNode splittedNode) {

        // create a new node to store (t-1) keys of splittedNode
        BusinessBNode newNode = new BusinessBNode(splittedNode.BMinDeg, splittedNode.BIsLeaf);
        newNode.BKeyNum = BMinDeg - 1;
        
        // copy the last (BMinDeg - 1) "keys" of splittedNode to newNode
        for (int i = 0; i < BMinDeg - 1; i++) {
            newNode.BKeys.set(i, splittedNode.BKeys.get(i+BMinDeg));
            splittedNode.BKeys.set(i+BMinDeg, new BusinessModel());
        }

        // copy the last BMinDeg "children" of splittedNode to newNode
        if (!splittedNode.BIsLeaf) {
            for (int i = 0; i < BMinDeg; i++) {
                newNode.BChild.set(i, splittedNode.BChild.get(i+BMinDeg));
                splittedNode.BChild.set(i+BMinDeg, new BusinessBNode());
            }
        }

        // reduce the number of keys in splittedNode
        splittedNode.BKeyNum = BMinDeg - 1;        
        
        // create new space for new child in this node
        for (int i = BKeyNum; i >= pos + 1; i --) {
            BChild.set(i+1, BChild.get(i));
        }
        
        // link the new child to newNode
        BChild.set(pos+1, newNode);

        // move a key from splittedNode to newNode. Find the location of new key and move all greater keys one space ahead
        for (int i =  BKeyNum - 1;i >= pos; i--) {
            BKeys.set(i+1, BKeys.get(i));
        }

        // copy the middle key of splittedNode to newNode
        BKeys.set(pos, splittedNode.BKeys.get(BMinDeg-1));
        splittedNode.BKeys.set(BMinDeg-1, new BusinessModel());

        // increment BKeyNum
        BKeyNum += 1;

    }
    
}
