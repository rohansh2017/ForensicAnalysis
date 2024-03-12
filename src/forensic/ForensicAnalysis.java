package forensic;

/**
 * This class represents a forensic analysis system that manages DNA data using
 * BSTs.
 * Contains methods to create, read, update, delete, and flag profiles.
 * 
 * @author Kal Pandit
 */
public class ForensicAnalysis {
    
    private TreeNode treeRoot; // BST's root
    private String firstUnknownSequence;
    private String secondUnknownSequence;

    public ForensicAnalysis() {
        treeRoot = null;
        firstUnknownSequence = null;
        secondUnknownSequence = null;
    }

    /**
     * Builds a simplified forensic analysis database as a BST and populates unknown
     * sequences.
     * The input file is formatted as follows:
     * 1. one line containing the number of people in the database, say p
     * 2. one line containing first unknown sequence
     * 3. one line containing second unknown sequence
     * 2. for each person (p), this method:
     * - reads the person's name
     * - calls buildSingleProfile to return a single profile.
     * - calls insertPerson on the profile built to insert into BST.
     * Use the BST insertion algorithm from class to insert.
     * 
     * DO NOT EDIT this method, IMPLEMENT buildSingleProfile and insertPerson.
     * 
     * @param filename the name of the file to read from
     */
    public void buildTree(String filename) {
        // DO NOT EDIT THIS CODE
        StdIn.setFile(filename); // DO NOT remove this line

        // Reads unknown sequences
        String sequence1 = StdIn.readLine();
        firstUnknownSequence = sequence1;
        String sequence2 = StdIn.readLine();
        secondUnknownSequence = sequence2;

        int numberOfPeople = Integer.parseInt(StdIn.readLine());

        for (int i = 0; i < numberOfPeople; i++) {
            // Reads name, count of STRs
            String fname = StdIn.readString();
            String lname = StdIn.readString();
            String fullName = lname + ", " + fname;
            // Calls buildSingleProfile to create
            Profile profileToAdd = createSingleProfile();
            // Calls insertPerson on that profile: inserts a key-value pair (name, profile)
            insertPerson(fullName, profileToAdd);
        }
    }

    /**
     * Reads ONE profile from input file and returns a new Profile.
     * Do not add a StdIn.setFile statement, that is done for you in buildTree.
     */
    public Profile createSingleProfile() {
        STR[] strs = new STR[StdIn.readInt()];
        for (int i = 0; i < strs.length; i++)
            strs[i] = new STR(StdIn.readString(), StdIn.readInt());
        return new Profile(strs); // update this line
    }

    /**
     * Inserts a node with a new (key, value) pair into
     * the binary search tree rooted at treeRoot.
     * 
     * Names are the keys, Profiles are the values.
     * USE the compareTo method on keys.
     * 
     * @param newProfile the profile to be inserted
     */
    public void insertPerson(String name, Profile newProfile) {
        TreeNode previous = null;
        TreeNode current = treeRoot;
        TreeNode person = new TreeNode(name, newProfile, null, null);
        if (treeRoot == null) {
            treeRoot = new TreeNode(name, newProfile, null, null);
            return;
        }

        while (current != null) {
            if (current.getName().compareTo(name) > 0) {
                previous = current;
                current = current.getLeft();
            } else {
                previous = current;
                current = current.getRight();
            }
        }

        if (previous.getName().compareTo(name) > 0)
            previous.setLeft(person);
        else
            previous.setRight(person);

    }

    /**
     * Finds the number of profiles in the BST whose interest status matches
     * isOfInterest.
     *
     * @param isOfInterest the search mode: whether we are searching for unmarked or
     *                     marked profiles. true if yes, false otherwise
     * @return the number of profiles according to the search mode marked
     */

    //helper method that I made to recursively traverse the BST.
    
    public int getMatchingProfileCount(TreeNode t, boolean isOfInterest){
        int count = 0;
        if (t == null) return 0;
        if (t.getProfile().getMarkedStatus() == isOfInterest)   count++;
        return count + getMatchingProfileCount(t.getLeft() ,isOfInterest)+ getMatchingProfileCount(t.getRight(), isOfInterest);
    } 

    public int getMatchingProfileCount(boolean isOfInterest) {
        return getMatchingProfileCount(treeRoot, isOfInterest);
    }

    /**
     * Helper method that counts the # of STR occurrences in a sequence.
     * Provided method - DO NOT UPDATE.
     * 
     * @param sequence the sequence to search
     * @param STR      the STR to count occurrences of
     * @return the number of times STR appears in sequence
     */
    private int numberOfOccurrences(String sequence, String STR) {

        // DO NOT EDIT THIS CODE

        int repeats = 0;
        // STRs can't be greater than a sequence
        if (STR.length() > sequence.length())
            return 0;

        // indexOf returns the first index of STR in sequence, -1 if not found
        int lastOccurrence = sequence.indexOf(STR);

        while (lastOccurrence != -1) {
            repeats++;
            // Move start index beyond the last found occurrence
            lastOccurrence = sequence.indexOf(STR, lastOccurrence + STR.length());
        }
        return repeats;
    }

    /**
     * Traverses the BST at treeRoot to mark profiles if:
     * - For each STR in profile STRs: at least half of STR occurrences match (round
     * UP)
     * - If occurrences THROUGHOUT DNA (first + second sequence combined) matches
     * occurrences, add a match
     */
    public void flagProfilesOfInterest(TreeNode t){
        if (t == null) return;

        STR[] strs = t.getProfile().getStrs();
        double halfLength = Math.ceil(strs.length*1.0/2);
        if (strs.length == 1)
            halfLength = 1;
        int count = 0;
        for (int i = 0; i < strs.length; i++){
            if ((numberOfOccurrences(firstUnknownSequence, strs[i].getStrString()) + numberOfOccurrences(secondUnknownSequence, strs[i].getStrString())) == strs[i].getOccurrences())
                count++;
        }
        if (count >= halfLength)
            t.getProfile().setInterestStatus(true);

        flagProfilesOfInterest(t.getLeft());
        flagProfilesOfInterest(t.getRight());
    }

    public void flagProfilesOfInterest() {
        flagProfilesOfInterest(treeRoot);
    }

    /**
     * Uses a level-order traversal to populate an array of unmarked Strings
     * representing unmarked people's names.
     * - USE the getMatchingProfileCount method to get the resulting array length.
     * - USE the provided Queue class to investigate a node and enqueue its
     * neighbors.
     * 
     * @return the array of unmarked people
     */
    public String[] getUnmarkedPeople() {
        Queue<TreeNode> q = new Queue<TreeNode>();
        String[] unmarked = new String[getMatchingProfileCount(false)];
        q.enqueue(treeRoot);
        int unmarkediterator = 0;
        while (!q.isEmpty()){
            TreeNode t = q.dequeue();
            if (!t.getProfile().getMarkedStatus())
                unmarked[unmarkediterator++] = t.getName();
            if (t.getLeft() != null)
                q.enqueue(t.getLeft());
            if (t.getRight() != null)
                q.enqueue(t.getRight());
        }
        return unmarked; // update this line
    }

    /**
     * Removes a SINGLE node from the BST rooted at treeRoot, given a full name
     * (Last, First)
     * This is similar to the BST delete we have seen in class.
     * 
     * If a profile containing fullName doesn't exist, do nothing.
     * You may assume that all names are distinct.
     * 
     * @param fullName the full name of the person to delete
     */

    private TreeNode min(TreeNode x){
        if (x.getLeft()== null) return x;
        return min(x.getLeft());
    }

    private TreeNode deleteMin(TreeNode x){
        if (x.getLeft() == null) return x.getRight();
        x.setLeft(deleteMin(x.getLeft()));
        return x;
    }
    public TreeNode removePerson(TreeNode t, String name){
        if (t == null) return null;
        int cmp = name.compareTo(t.getName());
        if (cmp < 0) t.setLeft(removePerson(t.getLeft(),name));
        else if (cmp > 0) t.setRight(removePerson(t.getRight(),name));
        else{
            if (t.getLeft() == null) return t.getRight();
            if (t.getRight() == null) return t.getLeft();

            TreeNode x = t;
            t = min(x.getRight());
            t.setRight(deleteMin(x.getRight()));
            t.setLeft(x.getLeft());
        }
        return t;
    }

    public void removePerson(String fullName) {
        treeRoot = removePerson(treeRoot, fullName);
    }

    /**
     * Clean up the tree by using previously written methods to remove unmarked
     * profiles.
     * Requires the use of getUnmarkedPeople and removePerson.
     */

    public void cleanupTree(TreeNode t){
        String[] x = getUnmarkedPeople();
        for (String i : x)
            removePerson(i);
    }

    public void cleanupTree() {
        cleanupTree(treeRoot);
    }

    /**
     * Gets the root of the binary search tree.
     *
     * @return The root of the binary search tree.
     */
    public TreeNode getTreeRoot() {
        return treeRoot;
    }

    /**
     * Sets the root of the binary search tree.
     *
     * @param newRoot The new root of the binary search tree.
     */
    public void setTreeRoot(TreeNode newRoot) {
        treeRoot = newRoot;
    }

    /**
     * Gets the first unknown sequence.
     * 
     * @return the first unknown sequence.
     */
    public String getFirstUnknownSequence() {
        return firstUnknownSequence;
    }

    /**
     * Sets the first unknown sequence.
     * 
     * @param newFirst the value to set.
     */
    public void setFirstUnknownSequence(String newFirst) {
        firstUnknownSequence = newFirst;
    }

    /**
     * Gets the second unknown sequence.
     * 
     * @return the second unknown sequence.
     */
    public String getSecondUnknownSequence() {
        return secondUnknownSequence;
    }

    /**
     * Sets the second unknown sequence.
     * 
     * @param newSecond the value to set.
     */
    public void setSecondUnknownSequence(String newSecond) {
        secondUnknownSequence = newSecond;
    }

}
