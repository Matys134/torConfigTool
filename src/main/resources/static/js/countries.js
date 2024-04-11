/**
 * An object representing countries with their respective codes.
 * @type {Object.<string, string>}
 */
let countries = {
    "auto": "Auto (recommended)",
    "nl": "Netherlands",
    "de": "Germany",
    "us": "United States",
    "pl": "Poland",
    "at": "Austria",
    "lu": "Luxembourg",
    "fr": "France",
    "no": "Norway",
    "ro": "Romania",
    "se": "Sweden",
    "dk": "Denmark",
    "ca": "Canada",
    "sw": "Switzerland",
    "ru": "Russia",
    "uk": "United Kingdom",
};

// Store the "auto" option separately before removing it from the countries object
const autoOption = {auto: countries.auto};
delete countries.auto;

// Convert the countries object into an array of key-value pairs
const countriesArray = Object.entries(countries);

// Sort the array by the country names
countriesArray.sort(function(a, b) {
    return a[1].localeCompare(b[1]);
});

// Convert the sorted array back into an object
const sortedCountries = Object.fromEntries(countriesArray);

// Add the "auto" option back to the top of the sorted countries object
countries = { ...autoOption, ...sortedCountries };