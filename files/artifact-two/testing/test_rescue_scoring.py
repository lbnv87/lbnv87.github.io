import pandas as pd

RESCUE_PROFILES = {
    "water": {
        "animal_type": "Dog",
        "breeds": [
            "Labrador Retriever Mix",
            "Chesapeake Bay Retriever",
            "Newfoundland"
        ],
        "sex": "Intact Female",
        "min_age": 26,
        "max_age": 156
    },
    "mountain": {
        "animal_type": "Dog",
        "breeds": [
            "German Shepherd",
            "Alaskan Malamute",
            "Old English Sheepdog",
            "Siberian Husky",
            "Rottweiler"
        ],
        "sex": "Intact Male",
        "min_age": 26,
        "max_age": 156
    },
    "disaster": {
        "animal_type": "Dog",
        "breeds": [
            "Doberman Pinscher",
            "German Shepherd",
            "Golden Retriever",
            "Bloodhound",
            "Rottweiler"
        ],
        "sex": "Intact Male",
        "min_age": 20,
        "max_age": 300
    }
}


def calculate_rescue_score(animal, rescue_type):
    if rescue_type not in RESCUE_PROFILES:
        return 0

    profile = RESCUE_PROFILES[rescue_type]
    score = 0

    if animal.get("animal_type") == profile["animal_type"]:
        score += 25

    if animal.get("breed") in profile["breeds"]:
        score += 35

    age = animal.get("age_upon_outcome_in_weeks")

    if age is not None:
        try:
            age = float(age)

            if profile["min_age"] <= age <= profile["max_age"]:
                score += 25

        except (ValueError, TypeError):
            pass

    if animal.get("sex_upon_outcome") == profile["sex"]:
        score += 15

    return score


def rank_animals(records, rescue_type):
    scored_animals = []

    for animal in records:
        animal_copy = animal.copy()
        score = calculate_rescue_score(animal_copy, rescue_type)

        if score > 0:
            animal_copy["match_score"] = score
            scored_animals.append(animal_copy)

    return sorted(
        scored_animals,
        key=lambda animal: animal.get("match_score", 0),
        reverse=True
    )


sample_animals = [
    {
        "name": "River",
        "animal_type": "Dog",
        "breed": "Labrador Retriever Mix",
        "sex_upon_outcome": "Intact Female",
        "age_upon_outcome_in_weeks": 52
    },
    {
        "name": "Rocky",
        "animal_type": "Dog",
        "breed": "German Shepherd",
        "sex_upon_outcome": "Intact Male",
        "age_upon_outcome_in_weeks": 90
    },
    {
        "name": "Misty",
        "animal_type": "Dog",
        "breed": "Newfoundland",
        "sex_upon_outcome": "Spayed Female",
        "age_upon_outcome_in_weeks": 200
    },
    {
        "name": "Shadow",
        "animal_type": "Cat",
        "breed": "Domestic Shorthair Mix",
        "sex_upon_outcome": "Neutered Male",
        "age_upon_outcome_in_weeks": 40
    },
    {
        "name": "BadData",
        "animal_type": "Dog",
        "breed": "Labrador Retriever Mix",
        "sex_upon_outcome": "Intact Female",
        "age_upon_outcome_in_weeks": "unknown"
    }
]


ranked_results = rank_animals(sample_animals, "water")

df = pd.DataFrame(ranked_results)
print(df[["name", "breed", "match_score"]])