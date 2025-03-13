import csv
import uuid
import sys

def transform_to_csv(input_file, output_file, language_code="en"):
    """
    Reads a text file with one word per line and transforms it into a CSV file with:
    - Column 1: Random UUID
    - Column 2: The word
    - Column 3: Language code

    Args:
        input_file (str): Path to the input text file
        output_file (str): Path to the output CSV file
        language_code (str): Language code to use for all words (default: "en")
    """
    try:
        # Read words from input file
        with open(input_file, 'r', encoding='utf-8') as f:
            words = [line.strip() for line in f if line.strip()]

        # Write to CSV
        with open(output_file, 'w', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)

            # Write header row (optional)
            writer.writerow(["UUID", "Word", "Language"])

            # Write data rows
            for word in words:
                random_uuid = str(uuid.uuid4())
                writer.writerow([random_uuid, word, language_code])

        print(f"Successfully transformed {len(words)} words to {output_file}")

    except FileNotFoundError:
        print(f"Error: Could not find the input file '{input_file}'")
        return False
    except Exception as e:
        print(f"Error: {e}")
        return False

    return True

if __name__ == "__main__":
    # You can modify these parameters as needed
    input_file_path = "words.txt"  # Change this to your input file
    output_file_path = "words.csv"  # Change this to your desired output file
    language_code = "en"  # Change this to your desired language code

    # If command line arguments are provided, use them
    if len(sys.argv) > 1:
        input_file_path = sys.argv[1]
    if len(sys.argv) > 2:
        output_file_path = sys.argv[2]
    if len(sys.argv) > 3:
        language_code = sys.argv[3]

    transform_to_csv(input_file_path, output_file_path, language_code)