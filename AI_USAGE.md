# AI Usage Log

As required by the ethical AI usage guideline (US 4.5), this document lists where AI
assistance was used while building the JustEat Food Ordering Application, and how the
output was verified.

| AI Tool | Where / how used | How output was verified |
|---|---|---|
| Claude (Anthropic) | Scaffolding the overall project structure (entities, repositories, services, controllers) from the assignment's user stories and database schema | Reviewed each class against the acceptance criteria in the user stories document; traced each field back to the schema table |
| Claude (Anthropic) | Generating the JWT authentication flow (`JwtUtil`, `JwtAuthFilter`, `SecurityConfig`) | Cross-checked against Spring Security's documented `OncePerRequestFilter` pattern; confirmed role-based `@PreAuthorize` rules match the CUSTOMER/RESTAURANT_OWNER matrix in the brief |
| Claude (Anthropic) | Generating unit test scaffolding (JUnit 5 + Mockito) for the service layer | Each test's scenario and expected outcome were checked against the assignment's own 15-test plan; assertions were reviewed for correctness rather than accepted as-is |
| Claude (Anthropic) | Drafting the React frontend (pages, context providers, API client) | Manually traced each page's data flow against the corresponding user story's acceptance criteria (e.g. order tracking polling interval, cart quantity controls) |
| Claude (Anthropic) | Drafting the Dockerfile and docker-compose configuration | Verified the multi-stage build follows the standard Maven-build → JRE-runtime pattern; confirmed service dependency ordering (MySQL health check before backend starts) |

## Note on this submission

This project was generated end-to-end with AI assistance based on a detailed assignment
document (architecture, schema, user stories, API design, and test plan) that a trainee
developer had already written. All generated code was reviewed for consistency with that
document. Some scope was deliberately trimmed for simplicity — see the "Scope & what's
simplified" section in `README.md` for the full list of what was left out and why.
