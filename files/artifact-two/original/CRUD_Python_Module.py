from pymongo import MongoClient
from bson.objectid import ObjectId
from pymongo.errors import PyMongoError

class AnimalShelter(object):
    """ CRUD operations for Animal collection in MongoDB """

    def __init__(self, username, password, HOST='localhost', PORT=27017, DB='AAC', COL='animals'):
        # User values
        USER = username
        PASS = password
        
        # Initialize Connection
        self.client = MongoClient(f"mongodb://{USER}:{PASS}@{HOST}:{PORT}")
        self.database = self.client[DB]
        self.collection = self.database[COL]
        

    # -----------------------------
    # CREATE (C of CRUD)
    # -----------------------------
    def create(self, data):
        """
        Inserts a document into the animals collection.
        Returns True if insertion is successful, otherwise False.
        """

        if data is None or type(data) is not dict:
            raise Exception("Nothing to save: data parameter is empty or not a dictionary.")

        try:
            result = self.collection.insert_one(data)
            return result.inserted_id is not None
        except PyMongoError as e:
            print("Create failed:", e)
            return False

    # -----------------------------
    # READ (R of CRUD)
    # -----------------------------
    def read(self, query):
        """
        Returns documents matching the query as a list.
        Must use find(), not find_one().
        """

        if query is None or type(query) is not dict:
            raise Exception("Query must be a non-empty dictionary.")

        try:
            result = list(self.collection.find(query))
            return result
        except PyMongoError as e:
            print("Read failed:", e)
            return []
        
    # -----------------------------
    # UPDATE (U of CRUD)
    # -----------------------------
    def update(self, query, new_values):
        """
        Update document(s) in the collection.
        Input:
            query      -> dictionary for selecting documents to update
            new_values -> dictionary of fields to change (without $set)
        Return: number of objects modified.
        """

        if query is None or not isinstance(query, dict):
            raise ValueError("query must be a non-empty dictionary.")
        if new_values is None or not isinstance(new_values, dict):
            raise ValueError("new_values must be a non-empty dictionary.")

        try:
            # Wrap new_values in $set so only those fields are updated
            update_doc = {"$set": new_values}
            result = self.collection.update_many(query, update_doc)
            return result.modified_count
        except PyMongoError as e:
            print("Update failed:", e)
            return 0

    # -----------------------------
    # DELETE (D of CRUD)
    # -----------------------------
    def delete(self, query):
        """
        Delete document(s) from the collection.
        Input:  query -> dictionary for selecting documents to remove
        Return: number of objects removed from the collection.
        """

        if query is None or not isinstance(query, dict):
            raise ValueError("query must be a non-empty dictionary.")

        try:
            result = self.collection.delete_many(query)
            return result.deleted_count
        except PyMongoError as e:
            print("Delete failed:", e)
            return 0